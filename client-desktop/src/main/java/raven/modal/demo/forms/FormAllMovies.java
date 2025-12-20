package raven.modal.demo.forms;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import raven.modal.Toast;
import raven.modal.demo.api.MovieApi;
import raven.modal.demo.dto.response.ApiResponse;
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

@SystemForm(name = "All Movies", description = "Browse all movies with async image loading")
public class FormAllMovies extends Form {

    private JPanel gridPanel;
    private JScrollPane scrollPane;
    private JTextField txtSearch;
    private JComboBox<String> cmbSort;
    private List<MovieResponse> movies = new ArrayList<>();
    private Map<Long, ImageIcon> imageCache = new HashMap<>();

    public FormAllMovies() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fillx,insets 15", "[fill]", "[]10[fill]"));

        // Header with search and sort
        add(createHeader());
        
        // Grid panel for movies
        gridPanel = new JPanel(new MigLayout("fillx,wrap 4,gap 15", "[fill,sg][fill,sg][fill,sg][fill,sg]"));
        gridPanel.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        scrollPane = new JScrollPane(gridPanel);
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
        loadMovies();
    }

    @Override
    public void formRefresh() {
        loadMovies();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new MigLayout("fillx,insets 0", "[grow]10[]10[]", "[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        JLabel title = new JLabel("Tất cả phim");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +5;");

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm phim...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:12;");
        txtSearch.addActionListener(e -> filterMovies());

        cmbSort = new JComboBox<>(new String[]{"Mới nhất", "Tên A-Z", "Năm phát hành"});
        cmbSort.putClientProperty(FlatClientProperties.STYLE, "arc:12;");
        cmbSort.addActionListener(e -> sortMovies());

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.putClientProperty(FlatClientProperties.STYLE, "arc:12;");
        btnRefresh.addActionListener(e -> loadMovies());

        panel.add(title, "split 2,aligny center");
        panel.add(new JLabel("(" + movies.size() + " phim)"), "aligny center");
        panel.add(txtSearch, "w 250!");
        panel.add(cmbSort, "w 150!");
        panel.add(btnRefresh, "w 100!");

        return panel;
    }

    private void loadMovies() {
        // Clear grid
        gridPanel.removeAll();
        
        // Show loading
        JLabel loading = new JLabel("Đang tải phim...", SwingConstants.CENTER);
        loading.putClientProperty(FlatClientProperties.STYLE, "font:+2;");
        gridPanel.add(loading, "span,grow");
        gridPanel.revalidate();
        gridPanel.repaint();

        // Load in background
        new SwingWorker<ApiResponse<List<MovieResponse>>, Void>() {
            @Override
            protected ApiResponse<List<MovieResponse>> doInBackground() {
                return MovieApi.getAllMovies();
            }

            @Override
            protected void done() {
                try {
                    ApiResponse<List<MovieResponse>> response = get();
                    if (response != null && response.getCode() == 200) {
                        movies = response.getResult();
                        displayMovies();
                        updateHeader();
                    } else {
                        showError("Lỗi tải phim: " + (response != null ? response.getMessage() : "Unknown"));
                    }
                } catch (Exception e) {
                    showError("Lỗi: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void displayMovies() {
        gridPanel.removeAll();

        if (movies.isEmpty()) {
            JLabel empty = new JLabel("Chưa có phim nào", SwingConstants.CENTER);
            empty.putClientProperty(FlatClientProperties.STYLE, "font:+2;foreground:$Label.disabledForeground;");
            gridPanel.add(empty, "span,grow");
        } else {
            for (MovieResponse movie : movies) {
                gridPanel.add(createMovieCard(movie));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createMovieCard(MovieResponse movie) {
        JPanel card = new JPanel(new MigLayout("wrap,fillx,insets 12", "[fill]"));
        card.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:16;" +
                "[light]background:tint($Panel.background,8%);" +
                "[dark]background:tint($Panel.background,4%);");

        // Placeholder image
        JLabel imgLabel = new JLabel("", SwingConstants.CENTER);
        imgLabel.setPreferredSize(new Dimension(200, 280));
        imgLabel.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:12;" +
                "[light]background:tint($Panel.background,15%);" +
                "[dark]background:tint($Panel.background,10%);");
        imgLabel.setOpaque(true);
        
        // Load image async
        loadImageAsync(movie, imgLabel);

        JLabel title = new JLabel("<html>" + movie.getTitle() + "</html>");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold;");

        JLabel genre = new JLabel(movie.getGenre() != null ? movie.getGenre().getName() : "N/A");
        genre.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        JLabel year = new JLabel("" + movie.getReleaseYear());
        year.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        JLabel status = new JLabel(movie.getStatusDisplay());
        status.setOpaque(true);
        status.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:999;border:4,8,4,8;" +
                "[light]background:tint($Panel.background,15%);" +
                "[dark]background:tint($Panel.background,10%);");

        JButton btnPlay = new JButton("Xem phim");
        btnPlay.putClientProperty(FlatClientProperties.STYLE, "arc:12;");
        btnPlay.setEnabled("PUBLISHED".equals(movie.getStatus()));
        btnPlay.addActionListener(e -> playMovie(movie));

        card.add(imgLabel, "growx");
        card.add(title, "growx");
        card.add(genre, "split 2");
        card.add(year);
        card.add(status, "growx");
        card.add(btnPlay, "growx");

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return card;
    }

    private void loadImageAsync(MovieResponse movie, JLabel imgLabel) {
        // Check cache first
        if (imageCache.containsKey(movie.getId())) {
            imgLabel.setIcon(imageCache.get(movie.getId()));
            imgLabel.setText("");
            return;
        }

        // Show loading text
        imgLabel.setText("⏳");
        imgLabel.putClientProperty(FlatClientProperties.STYLE, 
                imgLabel.getClientProperty(FlatClientProperties.STYLE) + "font:+10;");

        // Load in background
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
                        // Resize to fit
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
        // TODO: Implement video player
        JOptionPane.showMessageDialog(this,
                "Sẽ phát phim: " + movie.getTitle() + "\nURL: http://localhost:8080/api/hls/" + movie.getId() + "/master.m3u8",
                "Play Movie",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void filterMovies() {
        String search = txtSearch.getText().toLowerCase().trim();
        displayMovies(); // Will re-filter based on search
        // TODO: Implement actual filtering
    }

    private void sortMovies() {
        // TODO: Implement sorting
        displayMovies();
    }

    private void updateHeader() {
        // Rebuild header to update count
        removeAll();
        add(createHeader());
        add(scrollPane, "grow");
        revalidate();
        repaint();
    }

    private void showError(String message) {
        gridPanel.removeAll();
        JLabel error = new JLabel(message, SwingConstants.CENTER);
        error.putClientProperty(FlatClientProperties.STYLE, "font:+2;foreground:#ef4444;");
        gridPanel.add(error, "span,grow");
        gridPanel.revalidate();
        gridPanel.repaint();
        Toast.show(this, Toast.Type.ERROR, message);
    }
}
