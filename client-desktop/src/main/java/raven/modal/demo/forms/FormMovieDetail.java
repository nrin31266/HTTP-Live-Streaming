package raven.modal.demo.forms;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import raven.modal.demo.dto.response.MovieResponse;
import raven.modal.demo.system.Form;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class FormMovieDetail extends Form {
    
    private final MovieResponse movie;
    private ImageIcon movieImageIcon;
    private JLabel imageLabel;
    
    public FormMovieDetail(MovieResponse movie) {
        this.movie = movie;
        init();
    }
    
    private void init() {
        setLayout(new MigLayout("wrap,fillx,insets 20", "[fill]", "[]10[]"));
        
        // Title
        JLabel titleLabel = new JLabel(movie.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "foreground:$Component.accentColor");
        add(titleLabel, "wrap");
        
        // Main content panel with image and details
        JPanel contentPanel = new JPanel(new MigLayout("", "[300!]20[grow,fill]", "[grow,fill]"));
        
        // Left side - Movie poster
        JPanel imagePanel = new JPanel(new BorderLayout());
        imageLabel = new JLabel("⏳ Đang tải ảnh...", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 420));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        contentPanel.add(imagePanel);
        
        // Load image asynchronously
        loadMovieImage();
        
        // Right side - Movie details
        JPanel detailsPanel = new JPanel(new MigLayout("wrap 2,fillx", "[120!,right]10[grow,fill]", "[]10[]"));

        
        // Status
        String statusText = switch (movie.getStatus()) {
            case "PUBLISHED" -> "Đã xuất bản";
            case "DRAF" -> "Không hoạt động";
            case "PROCESSING" -> "Chưa sẵn sàng";
            case "FAILED" -> "Xử lý thất bại";
            default -> movie.getStatus();
        };
        addDetailRow(detailsPanel, "Trạng thái:", statusText);
        
        // Release year
        if (movie.getReleaseYear() != null) {
            addDetailRow(detailsPanel, "Năm phát hành:", String.valueOf(movie.getReleaseYear()));
        }
        
        // Duration
        if (movie.getDuration() != null) {
            addDetailRow(detailsPanel, "Thời lượng:", formatDuration(movie.getDuration()));
        }
        
        // Genres
        if (movie.getGenre() != null) {
            addDetailRow(detailsPanel, "Thể loại:", movie.getGenre().getName());
        }
        
        // Created date
        if (movie.getCreatedAt() != null) {
            addDetailRow(detailsPanel, "Ngày tạo:", movie.getCreatedAt().toString());
        }
        
        // Description (full width)
        if (movie.getDescription() != null && !movie.getDescription().isEmpty()) {
            detailsPanel.add(new JLabel("Mô tả:"), "aligny top");
            JTextArea descriptionArea = new JTextArea(movie.getDescription());
            descriptionArea.setEditable(false);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setRows(5);
            descriptionArea.putClientProperty(FlatClientProperties.STYLE, 
                "border:1,1,1,1,$Component.borderColor,1,10");
            JScrollPane scrollPane = new JScrollPane(descriptionArea);
            scrollPane.setPreferredSize(new Dimension(400, 120));
            detailsPanel.add(scrollPane, "growx");
        }
        
        // Video qualities (processing status)
        if (movie.getVideoQualities() != null && !movie.getVideoQualities().isEmpty()) {
            String qualitiesText = movie.getVideoQualities().stream()
                    .map(vq -> vq.getQuality() + " (" + vq.getResolution() + ")")
                    .collect(Collectors.joining(", "));
            addDetailRow(detailsPanel, "Chất lượng:", qualitiesText);
        }
        
        contentPanel.add(detailsPanel);
        add(contentPanel, "growx");
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 20 0 0 0", "[grow,right][]10[]"));
        
        // Favorite button (placeholder for now)
        JButton favoriteButton = new JButton("❤ Yêu thích");
        favoriteButton.putClientProperty(FlatClientProperties.STYLE, 
            "borderWidth:1;focusWidth:1;arc:10");
        favoriteButton.setEnabled(false); // Will be implemented later
        buttonPanel.add(favoriteButton);
        
        // Watch button
        JButton watchButton = new JButton("▶ Xem phim");
        watchButton.putClientProperty(FlatClientProperties.STYLE, 
            "borderWidth:0;focusWidth:1;arc:10;background:$Component.accentColor;foreground:#fff");
        
        // Check if movie is ready to watch
        boolean isReady = movie.getStatus().equals("PUBLISHED") && 
                         movie.getVideoQualities() != null &&
                         !movie.getVideoQualities().isEmpty();
        
        if (!isReady) {
            watchButton.setEnabled(false);
            watchButton.setText("⏳ Đang xử lý video");
        }
        
        watchButton.addActionListener(e -> {
            System.out.println("Watch button clicked for movie: " + movie.getTitle());
            try {
                VideoPlayerForm playerForm = new VideoPlayerForm();
                playerForm.setMovie(movie);
                raven.modal.demo.system.FormManager.showForm(playerForm);
            } catch (Exception ex) {
                System.err.println("Error opening video player: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Lỗi mở video player: " + ex.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(watchButton);
        add(buttonPanel, "growx");
    }
    
    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD));
        panel.add(labelComponent, "aligny top");
        
        JLabel valueComponent = new JLabel("<html>" + value + "</html>");
        panel.add(valueComponent, "growx");
    }
    
    private String formatDuration(Integer minutes) {
        if (minutes == null || minutes == 0) {
            return "N/A";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0) {
            return hours + " giờ " + mins + " phút";
        } else {
            return mins + " phút";
        }
    }
    
    private void loadMovieImage() {
        if (movie.getImageUrl() == null || movie.getImageUrl().isEmpty()) {
            imageLabel.setText("📽️ Không có ảnh");
            return;
        }
        
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                System.out.println("Loading movie detail image: " + movie.getImageUrl());
                
                URL url = new URL(movie.getImageUrl());
                java.net.URLConnection conn = url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                
                BufferedImage img = ImageIO.read(conn.getInputStream());
                
                if (img == null) {
                    System.err.println("ImageIO.read returned null for movie " + movie.getId());
                    return null;
                }
                
                System.out.println("Successfully loaded image for movie detail: " + movie.getId());
                
                // Scale image to fit 300x420 maintaining aspect ratio
                int targetWidth = 300;
                int targetHeight = 420;
                
                double aspectRatio = (double) img.getWidth() / img.getHeight();
                int scaledWidth = targetWidth;
                int scaledHeight = (int) (scaledWidth / aspectRatio);
                
                if (scaledHeight > targetHeight) {
                    scaledHeight = targetHeight;
                    scaledWidth = (int) (scaledHeight * aspectRatio);
                }
                
                Image scaledImage = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
            
            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        imageLabel.setIcon(icon);
                        imageLabel.setText(null);
                    } else {
                        imageLabel.setText("📽️ Lỗi tải ảnh");
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image for movie " + movie.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    imageLabel.setText("❌ Lỗi tải ảnh");
                }
            }
        };
        
        worker.execute();
    }
}
