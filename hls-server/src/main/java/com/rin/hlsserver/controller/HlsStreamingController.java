package com.rin.hlsserver.controller;

import com.rin.hlsserver.service.HlsStreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hls")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HlsStreamingController {

    private final HlsStreamingService hlsStreamingService;

    /**
     * GET /api/hls/{movieId}/master.m3u8
     * Lấy master playlist
     */
    @GetMapping("/{movieId}/master.m3u8")
    public ResponseEntity<Resource> getMasterPlaylist(@PathVariable Long movieId) {
        log.info("GET /api/hls/{}/master.m3u8", movieId);
        
        Resource resource = hlsStreamingService.getMasterPlaylist(movieId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"master.m3u8\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(resource);
    }

    /**
     * GET /api/hls/{movieId}/{quality}/playlist.m3u8
     * Lấy quality playlist (360p hoặc 720p)
     */
    @GetMapping("/{movieId}/{quality}/playlist.m3u8")
    public ResponseEntity<Resource> getQualityPlaylist(
            @PathVariable Long movieId,
            @PathVariable String quality) {
        
        log.info("GET /api/hls/{}/{}/playlist.m3u8", movieId, quality);
        
        Resource resource = hlsStreamingService.getQualityPlaylist(movieId, quality);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=\"%s_playlist.m3u8\"", quality))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(resource);
    }

    /**
     * GET /api/hls/{movieId}/{quality}/{segmentName}
     * Lấy video segment (.ts file)
     */
    @GetMapping("/{movieId}/{quality}/{segmentName:.+\\.ts}")
    public ResponseEntity<Resource> getSegment(
            @PathVariable Long movieId,
            @PathVariable String quality,
            @PathVariable String segmentName) {
        
        log.info("GET /api/hls/{}/{}/{}", movieId, quality, segmentName);
        
        Resource resource = hlsStreamingService.getSegment(movieId, quality, segmentName);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp2t"))
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=\"%s\"", segmentName))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000") // Cache segments 1 year
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }

    /**
     * GET /api/hls/{movieId}/ready
     * Kiểm tra movie có sẵn sàng để stream không
     */
    @GetMapping("/{movieId}/ready")
    public ResponseEntity<Boolean> checkMovieReady(@PathVariable Long movieId) {
        log.info("GET /api/hls/{}/ready", movieId);
        
        boolean ready = hlsStreamingService.isMovieReady(movieId);
        
        return ResponseEntity.ok(ready);
    }
}
