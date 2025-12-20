package raven.modal.demo.forms;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import raven.modal.Toast;
import raven.modal.demo.api.GenreApi;
import raven.modal.demo.api.MovieApi;
import raven.modal.demo.dto.response.ApiResponse;
import raven.modal.demo.dto.response.GenreResponse;
import raven.modal.demo.dto.response.MovieResponse;
import raven.modal.demo.system.Form;
import raven.modal.demo.utils.SystemForm;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SystemForm(name = "Genres", description = "Browse movies by genre")
public class FormGenres extends Form {

    private JPanel genresPanel;
    private JPanel moviesPanel;
    private JScrollPane scrollPane;
    private List<GenreResponse> genres = new ArrayList<>();
    private List<MovieResponse> allMovies = new ArrayList<>();
    private List<MovieResponse> filteredMovies = new ArrayList<>();
    private Long selectedGenreId = null;
    private Map<Long, ImageIcon> imageCache = new HashMap<>();

    public FormGenres() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fillx,insets 15", "[fill]", "[]15[fill]"));

        // Header
        JLabel title = new JLabel("Thể loại phim");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +5;");
        add(title);

        // Genres chips
        genresPanel = new JPanel(new MigLayout("insets 0,gap 8", "[]", "[]"));
        genresPanel.putClientProperty(FlatClientProperties.STYLE, "background:null;");
        add(genresPanel, "growx");

        // Movies grid
        moviesPanel = new JPanel(new MigLayout("fillx,wrap 4,gap 15", "[fill,sg][fill,sg][fill,sg][fill,sg]"));
        moviesPanel.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        scrollPane = new JScrollPane(moviesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "" +
                "width:5;" +
                "trackArc:$ScrollBar.thumbArc;" +
                "trackInsets:0,0,0,0;" +
                "thumbInsets:0,0,0,0;");

        add(scrollPane, "grow");
    }

    @Override
    public void formInit() {
        loadData();
    }

    @Override
    public void formRefresh() {
        loadData();
    }

    private void loadData() {
        // Show loading
        genresPanel.removeAll();
        genresPanel.add(new JLabel("Đang tải thể loại..."));
        genresPanel.revalidate();
        genresPanel.repaint();

        moviesPanel.removeAll();
        moviesPanel.add(new JLabel("Đang tải phim..."), "span");
        moviesPanel.revalidate();
        moviesPanel.repaint();

        // Load genres and movies
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    ApiResponse<List<GenreResponse>> genreResponse = GenreApi.getAllGenres();
                    if (genreResponse != null && genreResponse.getCode() == 200) {
                        genres = genreResponse.getResult();
                    }

                    ApiResponse<List<MovieResponse>> movieResponse = MovieApi.getAllMovies();
                    if (movieResponse != null && movieResponse.getCode() == 200) {
                        allMovies = movieResponse.getResult();
                        filteredMovies = new ArrayList<>(allMovies);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                displayGenres();
                displayMovies();
            }
        }.execute();
    }

    private void displayGenres() {
        genresPanel.removeAll();

        // "All" button
        JButton btnAll = createGenreChip("Tất cả", null);
        genresPanel.add(btnAll);

        // Genre buttons
        for (GenreResponse genre : genres) {
            JButton btn = createGenreChip(genre.getName(), genre.getId());
            genresPanel.add(btn);
        }

        genresPanel.revalidate();
        genresPanel.repaint();
    }

    private JButton createGenreChip(String name, Long genreId) {
        JButton btn = new JButton(name);
        
        boolean isSelected = (genreId == null && selectedGenreId == null) || 
                            (genreId != null && genreId.equals(selectedGenreId));

        btn.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:999;" +
                "margin:8,16,8,16;" +
                (isSelected 
                    ? "background:$Component.accentColor;foreground:$Panel.background;" 
                    : "[light]background:tint($Panel.background,12%);" +
                      "[dark]background:tint($Panel.background,8%);"));

        btn.addActionListener(e -> {
            selectedGenreId = genreId;
            filterByGenre(genreId);
            displayGenres(); // Refresh to update selected state
        });

        return btn;
    }

    private void filterByGenre(Long genreId) {
        if (genreId == null) {
            filteredMovies = new ArrayList<>(allMovies);
        } else {
            filteredMovies = new ArrayList<>();
            for (MovieResponse movie : allMovies) {
                if (movie.getGenre() != null && genreId.equals(movie.getGenre().getId())) {
                    filteredMovies.add(movie);
                }
            }
        }
        displayMovies();
    }

    private void displayMovies() {
        moviesPanel.removeAll();

        if (filteredMovies.isEmpty()) {
            JLabel empty = new JLabel("Không có phim nào trong thể loại này", SwingConstants.CENTER);
            empty.putClientProperty(FlatClientProperties.STYLE, "font:+2;foreground:$Label.disabledForeground;");
            moviesPanel.add(empty, "span,grow");
        } else {
            for (MovieResponse movie : filteredMovies) {
                moviesPanel.add(createMovieCard(movie));
            }
        }

        moviesPanel.revalidate();
        moviesPanel.repaint();
    }

    private JPanel createMovieCard(MovieResponse movie) {
        JPanel card = new JPanel(new MigLayout("wrap,fillx,insets 12", "[fill]"));
        card.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:16;" +
                "[light]background:tint($Panel.background,8%);" +
                "[dark]background:tint($Panel.background,4%);");

        // Image
        JLabel imgLabel = new JLabel("", SwingConstants.CENTER);
        imgLabel.setPreferredSize(new Dimension(200, 280));
        imgLabel.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:12;" +
                "[light]background:tint($Panel.background,15%);" +
                "[dark]background:tint($Panel.background,10%);");
        imgLabel.setOpaque(true);
        
        loadImageAsync(movie, imgLabel);

        JLabel title = new JLabel("<html>" + movie.getTitle() + "</html>");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold;");

        JLabel year = new JLabel("" + movie.getReleaseYear());
        year.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        JButton btnPlay = new JButton("Xem phim");
        btnPlay.putClientProperty(FlatClientProperties.STYLE, "arc:12;");
        btnPlay.setEnabled("PUBLISHED".equals(movie.getStatus()));
        btnPlay.addActionListener(e -> playMovie(movie));

        card.add(imgLabel, "growx");
        card.add(title, "growx");
        card.add(year, "growx");
        card.add(btnPlay, "growx");

        return card;
    }

    private void loadImageAsync(MovieResponse movie, JLabel imgLabel) {
        if (imageCache.containsKey(movie.getId())) {
            imgLabel.setIcon(imageCache.get(movie.getId()));
            imgLabel.setText("");
            return;
        }

        imgLabel.setText("⏳");
        imgLabel.putClientProperty(FlatClientProperties.STYLE, 
                imgLabel.getClientProperty(FlatClientProperties.STYLE) + "font:+10;");

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    if (movie.getImageUrl() == null || movie.getImageUrl().isEmpty()) {
                        System.err.println("Movie " + movie.getId() + " has no image URL");
                        return null;
                    }

                    System.out.println("Loading image for movie " + movie.getId() + ": " + movie.getImageUrl());
                    
                    URL url = new URL(movie.getImageUrl());
                    java.net.URLConnection conn = url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    
                    BufferedImage img = ImageIO.read(conn.getInputStream());
                    
                    if (img != null) {
                        System.out.println("Successfully loaded image for movie " + movie.getId());
                        Image scaled = img.getScaledInstance(200, 280, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    } else {
                        System.err.println("ImageIO.read returned null for movie " + movie.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image for movie " + movie.getId() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        imgLabel.setIcon(icon);
                        imgLabel.setText("");
                        imageCache.put(movie.getId(), icon);
                    } else {
                        imgLabel.setText("📽️");
                        imgLabel.putClientProperty(FlatClientProperties.STYLE, 
                                imgLabel.getClientProperty(FlatClientProperties.STYLE) + "font:+20;");
                    }
                } catch (Exception e) {
                    imgLabel.setText("❌");
                    System.err.println("Error in done() for movie " + movie.getId() + ": " + e.getMessage());
                }
            }
        }.execute();
    }

    private void playMovie(MovieResponse movie) {
        JOptionPane.showMessageDialog(this,
                "Sẽ phát phim: " + movie.getTitle() + "\nURL: http://localhost:8080/api/hls/" + movie.getId() + "/master.m3u8",
                "Play Movie",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
