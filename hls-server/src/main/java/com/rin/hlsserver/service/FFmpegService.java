package com.rin.hlsserver.service;

import com.rin.hlsserver.model.Movie;
import com.rin.hlsserver.model.VideoQuality;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class FFmpegService {
    
    @Value("${app.hls.storage-path:/home/nrin31266/hls-data/videos/hls}")
    private String hlsStoragePath;
    
    @Value("${app.ffmpeg.use-cuda:true}")
    private boolean useCuda;
    
    @Value("${app.ffmpeg.segment-duration:6}")
    private int segmentDuration; // seconds per segment
    
    /**
     * Xử lý video gốc thành HLS với nhiều chất lượng
     * 
     * @param movie Movie entity
     * @param sourceVideoPath Đường dẫn video gốc
     * @param processingMinutes Số phút muốn xử lý (0 = skip, > duration = all)
     * @return List<VideoQuality> đã tạo
     */
    public List<VideoQuality> processVideoToHLS(Movie movie, String sourceVideoPath, int processingMinutes, 
                                                 Consumer<Integer> progressCallback) throws Exception {
        log.info("Starting FFmpeg processing for movie ID: {}, processingMinutes: {}", movie.getId(), processingMinutes);
        
        // Nếu processingMinutes = 0, skip processing
        if (processingMinutes == 0) {
            log.info("Processing minutes is 0, skipping video processing");
            return new ArrayList<>();
        }
        
        // Kiểm tra file source tồn tại
        File sourceFile = new File(sourceVideoPath);
        if (!sourceFile.exists()) {
            throw new FileNotFoundException("Source video not found: " + sourceVideoPath);
        }
        
        // Tạo thư mục output cho movie
        String movieDir = hlsStoragePath + "/" + movie.getId();
        Files.createDirectories(Paths.get(movieDir));
        
        // Lấy thông tin video
        VideoInfo videoInfo = getVideoInfo(sourceVideoPath);
        log.info("Video info - Duration: {} seconds", videoInfo.durationSeconds);
        
        // Tính toán duration cần xử lý
        int targetDurationSeconds;
        if (processingMinutes >= movie.getDuration()) {
            // Xử lý hết
            targetDurationSeconds = (int) videoInfo.durationSeconds;
        } else {
            // Xử lý một phần
            targetDurationSeconds = processingMinutes * 60;
        }
        
        log.info("Will process {} seconds out of {} total seconds", 
                targetDurationSeconds, videoInfo.durationSeconds);
        
        // Xử lý từng chất lượng
        List<VideoQuality> qualities = new ArrayList<>();
        
        // 360p (0-50% progress)
        VideoQuality q360 = processQuality(movie, sourceVideoPath, VideoQuality.Quality.Q360P, 
                movieDir, targetDurationSeconds, (int) videoInfo.durationSeconds, 
                progress -> progressCallback.accept(progress / 2));
        if (q360 != null) {
            qualities.add(q360);
        }
        
        // 720p (50-100% progress)
        VideoQuality q720 = processQuality(movie, sourceVideoPath, VideoQuality.Quality.Q720P, 
                movieDir, targetDurationSeconds, (int) videoInfo.durationSeconds,
                progress -> progressCallback.accept(50 + progress / 2));
        if (q720 != null) {
            qualities.add(q720);
        }
        
        // Tạo master playlist
        String masterPlaylistPath = createMasterPlaylist(movieDir, qualities);
        movie.setMasterPlaylistPath(masterPlaylistPath);
        
        log.info("FFmpeg processing completed successfully for movie ID: {}", movie.getId());
        return qualities;
    }
    
    /**
     * Xử lý một chất lượng cụ thể
     */
    private VideoQuality processQuality(Movie movie, String sourceVideoPath, 
                                       VideoQuality.Quality quality, String movieDir,
                                       int durationSeconds, int totalDuration,
                                       Consumer<Integer> progressCallback) throws Exception {
        
        log.info("Processing quality: {}", quality.getLabel());
        
        // Tạo thư mục cho chất lượng này
        String qualityDir = movieDir + "/" + quality.getLabel();
        Files.createDirectories(Paths.get(qualityDir));
        
        String playlistPath = qualityDir + "/playlist.m3u8";
        
        // Build FFmpeg command
        List<String> command = buildFFmpegCommand(sourceVideoPath, qualityDir, quality, durationSeconds);
        
        // Execute FFmpeg with progress tracking
        boolean success = executeFFmpeg(command, totalDuration, progressCallback);
        
        if (!success) {
            log.error("Failed to process quality: {}", quality.getLabel());
            return null;
        }
        
        // Tính file size
        long totalSize = calculateDirectorySize(Paths.get(qualityDir));
        
        return VideoQuality.builder()
                .movie(movie)
                .quality(quality)
                .playlistPath(playlistPath)
                .segmentsPath(qualityDir)
                .bitrate(quality.getBitrate())
                .resolution(quality.getResolution())
                .fileSize(totalSize)
                .build();
    }
    
    /**
     * Build FFmpeg command với CUDA acceleration
     */
    private List<String> buildFFmpegCommand(String inputPath, String outputDir, 
                                           VideoQuality.Quality quality, int durationSeconds) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y"); // Overwrite output files
        
        // Input
        command.add("-i");
        command.add(inputPath);
        
        // Duration limit
        command.add("-t");
        command.add(String.valueOf(durationSeconds));
        
        // Video codec and encoding settings
        if (useCuda) {
            // Use NVIDIA GPU encoder (no -hwaccel needed, encoder is enough)
            command.add("-c:v");
            command.add("h264_nvenc");
            command.add("-preset");
            command.add("fast"); // NVENC preset
        } else {
            command.add("-c:v");
            command.add("libx264"); // Software encoder
            command.add("-preset");
            command.add("medium"); // x264 preset
        }
        
        // Video filter - scale with aspect ratio maintained
        command.add("-vf");
        command.add(String.format("scale=-2:%d", quality.getHeight())); // -2 for even width
        command.add("-b:v");
        command.add(quality.getBitrate() + "k");
        command.add("-maxrate");
        command.add((quality.getBitrate() + 500) + "k");
        command.add("-bufsize");
        command.add((quality.getBitrate() * 2) + "k");
        
        // Audio settings
        command.add("-c:a");
        command.add("aac");
        command.add("-b:a");
        command.add("128k");
        command.add("-ar");
        command.add("44100");
        
        // HLS settings
        command.add("-f");
        command.add("hls");
        command.add("-hls_time");
        command.add(String.valueOf(segmentDuration));
        command.add("-hls_list_size");
        command.add("0"); // Keep all segments in playlist
        command.add("-hls_segment_filename");
        command.add(outputDir + "/segment_%03d.ts");
        
        // Output playlist
        command.add(outputDir + "/playlist.m3u8");
        
        return command;
    }
    
    /**
     * Execute FFmpeg command with progress tracking
     */
    private boolean executeFFmpeg(List<String> command, int totalDuration, 
                                 Consumer<Integer> progressCallback) throws IOException, InterruptedException {
        log.info("Executing FFmpeg command: {}", String.join(" ", command));
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false); // Separate stderr for progress parsing
        
        Process process = pb.start();
        
        // Pattern to match FFmpeg progress: time=00:01:23.45
        Pattern timePattern = Pattern.compile("time=(\\d+):(\\d+):(\\d+\\.\\d+)");
        
        // Read stderr in separate thread for progress
        Thread progressThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("FFmpeg: {}", line);
                    
                    // Parse progress
                    Matcher matcher = timePattern.matcher(line);
                    if (matcher.find()) {
                        int hours = Integer.parseInt(matcher.group(1));
                        int minutes = Integer.parseInt(matcher.group(2));
                        double seconds = Double.parseDouble(matcher.group(3));
                        
                        int currentSeconds = hours * 3600 + minutes * 60 + (int) seconds;
                        int progress = Math.min(100, (currentSeconds * 100) / totalDuration);
                        
                        if (progressCallback != null) {
                            progressCallback.accept(progress);
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Error reading FFmpeg stderr", e);
            }
        });
        progressThread.start();
        
        // Read stdout
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("FFmpeg stdout: {}", line);
            }
        }
        
        boolean finished = process.waitFor(30, TimeUnit.MINUTES);
        
        if (!finished) {
            process.destroyForcibly();
            log.error("FFmpeg process timeout");
            return false;
        }
        
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("FFmpeg process failed with exit code: {}", exitCode);
            return false;
        }
        
        return true;
    }
    
    /**
     * Lấy thông tin video
     */
    private VideoInfo getVideoInfo(String videoPath) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffprobe");
        command.add("-v");
        command.add("error");
        command.add("-show_entries");
        command.add("format=duration");
        command.add("-of");
        command.add("default=noprint_wrappers=1:nokey=1");
        command.add(videoPath);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        String durationStr;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            durationStr = reader.readLine();
        }
        
        process.waitFor();
        
        double duration = durationStr != null ? Double.parseDouble(durationStr) : 0.0;
        
        return new VideoInfo(duration);
    }
    
    /**
     * Tạo master playlist
     */
    private String createMasterPlaylist(String movieDir, List<VideoQuality> qualities) throws IOException {
        String masterPath = movieDir + "/master.m3u8";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(masterPath))) {
            writer.write("#EXTM3U\n");
            writer.write("#EXT-X-VERSION:3\n\n");
            
            for (VideoQuality q : qualities) {
                writer.write(String.format("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%s\n",
                        q.getBitrate() * 1000, q.getResolution()));
                writer.write(q.getQuality().getLabel() + "/playlist.m3u8\n\n");
            }
        }
        
        log.info("Created master playlist: {}", masterPath);
        return masterPath;
    }
    
    /**
     * Tính tổng size của thư mục
     */
    private long calculateDirectorySize(Path directory) {
        try {
            return Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .mapToLong(file -> {
                        try {
                            return Files.size(file);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            log.error("Error calculating directory size", e);
            return 0L;
        }
    }
    
    /**
     * Xóa tất cả video files của một movie
     */
    public void deleteMovieVideos(Long movieId) {
        String movieDir = hlsStoragePath + "/" + movieId;
        try {
            Path path = Paths.get(movieDir);
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted((a, b) -> -a.compareTo(b)) // Delete files before directories
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                log.error("Failed to delete: {}", p, e);
                            }
                        });
                log.info("Deleted video directory: {}", movieDir);
            }
        } catch (IOException e) {
            log.error("Error deleting movie videos", e);
        }
    }
    
    private static class VideoInfo {
        final double durationSeconds;
        
        VideoInfo(double durationSeconds) {
            this.durationSeconds = durationSeconds;
        }
    }
}
