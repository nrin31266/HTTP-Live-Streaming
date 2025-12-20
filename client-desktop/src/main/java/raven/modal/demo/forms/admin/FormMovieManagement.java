package raven.modal.demo.forms.admin;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.demo.model.Genre;
import raven.modal.demo.model.Movie;
import raven.modal.demo.system.Form;
import raven.modal.demo.utils.GenreManager;
import raven.modal.demo.utils.SystemForm;
import raven.modal.option.Location;
import raven.modal.option.Option;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@SystemForm(name = "Movie Management", description = "Quản lý danh sách phim")
public class FormMovieManagement extends Form {

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Movie> movies;

    public FormMovieManagement() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fillx,wrap,insets 15", "[fill]", "[][fill,grow]"));

        // Header với nút thêm phim
        JPanel headerPanel = new JPanel(new MigLayout("fillx,insets 0", "[]push[]"));

        JLabel title = new JLabel("Quản Lý Phim");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");

        JButton btnAdd = new JButton("Thêm Phim Mới");
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "arc:8");
        btnAdd.setIcon(new FlatSVGIcon("raven/modal/demo/icons/add.svg", 0.5f));
        btnAdd.addActionListener(e -> showAddMovieDialog());

        headerPanel.add(title);
        headerPanel.add(btnAdd);

        // Table
        String[] columns = {"ID", "Tên Phim", "Thể Loại", "Năm", "Thời Lượng", "Trạng Thái", "Hành Động"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Chỉ cột hành động có thể click
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE,
                "height:40;font:bold;");
        table.putClientProperty(FlatClientProperties.STYLE,
                "showHorizontalLines:true;" +
                        "intercellSpacing:0,1;" +
                        "showVerticalLines:false;");

        // Renderer cho các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Năm
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Thời lượng

        // Status renderer với màu sắc
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);

                String status = value.toString();
                if ("published".equals(status)) {
                    label.setForeground(new Color(34, 197, 94)); // green
                } else if ("processing".equals(status)) {
                    label.setForeground(new Color(234, 179, 8)); // yellow
                } else {
                    label.setForeground(new Color(156, 163, 175)); // gray
                }
                return label;
            }
        });

        // Action column với nút
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor());

        // Đặt độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder()
        ));

        add(headerPanel);
        add(scrollPane, "grow");
    }

    @Override
    public void formInit() {
        loadMovies();
    }

    private void loadMovies() {
        // Dữ liệu tĩnh mẫu
        movies = new ArrayList<>();
        movies.add(new Movie(1, "Nhà Tù Shawshank", "Hai tù nhân tạo nên tình bạn qua nhiều năm",
                "https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg",
                "/videos/shawshank.m3u8", 142, 5, 1994, "drama", "published"));
        movies.add(new Movie(2, "Bố Già", "Ông trùm mafia già nua của một gia đình tội phạm",
                "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
                "/videos/godfather.m3u8", 175, 10, 1972, "crime", "published"));
        movies.add(new Movie(3, "Kỵ Sĩ Bóng Đêm", "Batman đối đầu với Joker",
                "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
                "/videos/darkknight.m3u8", 152, 8, 2008, "action", "published"));
        movies.add(new Movie(4, "Pulp Fiction", "Cuộc sống của hai sát thủ mafia",
                "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
                "/videos/pulpfiction.m3u8", 154, 0, 1994, "crime", "published"));
        movies.add(new Movie(5, "Kẻ Cắp Giấc Mơ", "Tên trộm đánh cắp bí mật qua giấc mơ",
                "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
                "/videos/inception.m3u8", 148, 15, 2010, "sci-fi", "processing"));
        movies.add(new Movie(6, "Câu Lạc Bộ Chiến Đấu", "Nhân viên văn phòng mất ngủ",
                "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
                "/videos/fightclub.m3u8", 139, 0, 1999, "drama", "draft"));

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Movie movie : movies) {
            // Tìm tên tiếng Việt của genre
            String genreName = movie.getGenre();
            Genre[] genreArray = GenreManager.getInstance().getGenreArray();
            for (Genre g : genreArray) {
                if (g.getId().equals(movie.getGenre())) {
                    genreName = g.getName();
                    break;
                }
            }

            tableModel.addRow(new Object[]{
                    movie.getId(),
                    movie.getTitle(),
                    genreName,
                    movie.getReleaseYear(),
                    movie.getDurationDisplay(),
                    movie.getStatus(),
                    movie // Pass movie object for action buttons
            });
        }
    }

    private void showAddMovieDialog() {
        JPanel panel = new JPanel(new MigLayout("fillx,wrap,insets 20", "[right]15[fill,300]"));

        JLabel lblTitle = new JLabel("Thêm Phim Mới");
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");

        JTextField txtTitle = new JTextField();
        JTextArea txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);

        JTextField txtImageUrl = new JTextField();
        JTextField txtVideoUrl = new JTextField();
        JTextField txtDuration = new JTextField();
        JTextField txtProcessingMinutes = new JTextField("0");
        int currentYear = Year.now().getValue();
        JSpinner spinYear = new JSpinner(new SpinnerNumberModel(currentYear, 1900, 2100, 1));
        JComboBox<Genre> cboGenre = new JComboBox<>(GenreManager.getInstance().getGenreArray());
        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"draft", "processing", "published"});

        // Style cho các components
        txtTitle.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên phim");
        txtDescription.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mô tả phim");
        txtImageUrl.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "https://example.com/image.jpg");
        txtVideoUrl.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "/videos/movie.m3u8");
        txtDuration.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "142 (phút)");
        txtProcessingMinutes.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "0 = không xử lý");

        panel.add(lblTitle, "span 2,center,gapbottom 15");
        panel.add(new JLabel("Tên Phim:"));
        panel.add(txtTitle);
        panel.add(new JLabel("Mô Tả:"));
        panel.add(scrollDesc);
        panel.add(new JLabel("Image URL:"));
        panel.add(txtImageUrl);
        panel.add(new JLabel("Video URL:"));
        panel.add(txtVideoUrl);
        panel.add(new JLabel("Thời Lượng (phút):"));
        panel.add(txtDuration);
        panel.add(new JLabel("Xử Lý Video (phút):"));
        panel.add(txtProcessingMinutes);
        panel.add(new JLabel("Năm Phát Hành:"));
        panel.add(spinYear);
        panel.add(new JLabel("Thể Loại:"));
        panel.add(cboGenre);
        panel.add(new JLabel("Trạng Thái:"));
        panel.add(cboStatus);

        Option option = ModalDialog.createOption();
        option.getLayoutOption().setSize(500, -1)
                .setLocation(Location.CENTER, Location.CENTER)
                .setAnimateDistance(0.7f, 0);

        ModalDialog.showModal(this, new SimpleModalBorder(
                panel, "Thêm Phim Mới", SimpleModalBorder.YES_NO_OPTION,
                (controller, action) -> {
                    if (action == SimpleModalBorder.YES_OPTION) {
                        // Validate
                        if (txtTitle.getText().trim().isEmpty()) {
                            controller.consume();
                            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên phim!",
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        try {
                            // Tạo movie mới
                            Movie movie = new Movie();
                            movie.setId(movies.size() + 1);
                            movie.setTitle(txtTitle.getText().trim());
                            movie.setDescription(txtDescription.getText().trim());
                            movie.setImageUrl(txtImageUrl.getText().trim());
                            movie.setVideoUrl(txtVideoUrl.getText().trim());
                            movie.setDuration(Integer.parseInt(txtDuration.getText().trim()));
                            movie.setProcessingMinutes(Integer.parseInt(txtProcessingMinutes.getText().trim()));
                            movie.setReleaseYear((Integer) spinYear.getValue());
                            Genre selectedGenre = (Genre) cboGenre.getSelectedItem();
                            movie.setGenre(selectedGenre != null ? selectedGenre.getId() : "");
                            movie.setStatus(cboStatus.getSelectedItem().toString());

                            movies.add(movie);
                            refreshTable();

                            String msg = "Đã thêm phim thành công!";
                            if (movie.getProcessingMinutes() > 0) {
                                msg += "\n(Backend sẽ xử lý " + movie.getProcessingMinutes() + " phút video với FFmpeg)";
                            }
                            JOptionPane.showMessageDialog(this, msg,
                                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);

                        } catch (NumberFormatException e) {
                            controller.consume();
                            JOptionPane.showMessageDialog(this, "Thời lượng phải là số!",
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }), option);
    }

    private void showEditMovieDialog(Movie movie) {
        JPanel panel = new JPanel(new MigLayout("fillx,wrap,insets 20", "[right]15[fill,300]"));

        JLabel lblTitle = new JLabel("Chỉnh Sửa Phim");
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");

        JTextField txtTitle = new JTextField(movie.getTitle());
        JTextArea txtDescription = new JTextArea(movie.getDescription(), 3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);

        JTextField txtImageUrl = new JTextField(movie.getImageUrl());
        JTextField txtVideoUrl = new JTextField(movie.getVideoUrl());
        JTextField txtDuration = new JTextField(movie.getDuration().toString());
        JTextField txtProcessingMinutes = new JTextField(movie.getProcessingMinutes() != null ?
                movie.getProcessingMinutes().toString() : "0");
        int releaseYear = movie.getReleaseYear() != null ? movie.getReleaseYear() : Year.now().getValue();
        JSpinner spinYear = new JSpinner(new SpinnerNumberModel(releaseYear, 1900, 2100, 1));
        JComboBox<Genre> cboGenre = new JComboBox<>(GenreManager.getInstance().getGenreArray());
        // Tìm và select genre hiện tại
        Genre[] genreArray = GenreManager.getInstance().getGenreArray();
        for (Genre g : genreArray) {
            if (g.getId().equals(movie.getGenre())) {
                cboGenre.setSelectedItem(g);
                break;
            }
        }
        JComboBox<String> cboStatus = new JComboBox<>(new String[]{"draft", "processing", "published"});
        cboStatus.setSelectedItem(movie.getStatus());

        panel.add(lblTitle, "span 2,center,gapbottom 15");
        panel.add(new JLabel("Tên Phim:"));
        panel.add(txtTitle);
        panel.add(new JLabel("Mô Tả:"));
        panel.add(scrollDesc);
        panel.add(new JLabel("Image URL:"));
        panel.add(txtImageUrl);
        panel.add(new JLabel("Video URL:"));
        panel.add(txtVideoUrl);
        panel.add(new JLabel("Thời Lượng (phút):"));
        panel.add(txtDuration);
        panel.add(new JLabel("Xử Lý Video (phút):"));
        panel.add(txtProcessingMinutes);
        panel.add(new JLabel("Năm Phát Hành:"));
        panel.add(spinYear);
        panel.add(new JLabel("Thể Loại:"));
        panel.add(cboGenre);
        panel.add(new JLabel("Trạng Thái:"));
        panel.add(cboStatus);

        Option option = ModalDialog.createOption();
        option.getLayoutOption().setSize(500, -1)
                .setLocation(Location.CENTER, Location.CENTER)
                .setAnimateDistance(0.7f, 0);

        ModalDialog.showModal(this, new SimpleModalBorder(
                panel, "Chỉnh Sửa Phim", SimpleModalBorder.YES_NO_OPTION,
                (controller, action) -> {
                    if (action == SimpleModalBorder.YES_OPTION) {
                        if (txtTitle.getText().trim().isEmpty()) {
                            controller.consume();
                            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên phim!",
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        try {
                            movie.setTitle(txtTitle.getText().trim());
                            movie.setDescription(txtDescription.getText().trim());
                            movie.setImageUrl(txtImageUrl.getText().trim());
                            movie.setVideoUrl(txtVideoUrl.getText().trim());
                            movie.setDuration(Integer.parseInt(txtDuration.getText().trim()));
                            movie.setProcessingMinutes(Integer.parseInt(txtProcessingMinutes.getText().trim()));
                            movie.setReleaseYear((Integer) spinYear.getValue());
                            Genre selectedGenre = (Genre) cboGenre.getSelectedItem();
                            movie.setGenre(selectedGenre != null ? selectedGenre.getId() : "");
                            movie.setStatus(cboStatus.getSelectedItem().toString());

                            refreshTable();
                            JOptionPane.showMessageDialog(this, "Đã cập nhật phim thành công!",
                                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);

                        } catch (NumberFormatException e) {
                            controller.consume();
                            JOptionPane.showMessageDialog(this, "Thời lượng phải là số!",
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }), option);
    }

    private void deleteMovie(Movie movie) {
        int result = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa phim \"" + movie.getTitle() + "\"?",
                "Xác Nhận Xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            movies.remove(movie);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Đã xóa phim thành công!",
                    "Thành Công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Renderer cho cột hành động
    private class ActionButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btnEdit;
        private final JButton btnDelete;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            setOpaque(true);

            btnEdit = new JButton("Sửa");
            btnEdit.putClientProperty(FlatClientProperties.STYLE, "arc:5;border:2,8,2,8");
            btnEdit.setIcon(new FlatSVGIcon("raven/modal/demo/icons/edit.svg", 0.35f));

            btnDelete = new JButton("Xóa");
            btnDelete.putClientProperty(FlatClientProperties.STYLE, "arc:5;border:2,8,2,8");
            btnDelete.setIcon(new FlatSVGIcon("raven/modal/demo/icons/delete.svg", 0.35f));

            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    // Editor cho cột hành động
    private class ActionButtonEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel;
        private final JButton btnEdit;
        private final JButton btnDelete;
        private Movie currentMovie;

        public ActionButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));

            btnEdit = new JButton("Sửa");
            btnEdit.putClientProperty(FlatClientProperties.STYLE, "arc:5;border:2,8,2,8");
            btnEdit.setIcon(new FlatSVGIcon("raven/modal/demo/icons/edit.svg", 0.35f));
            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                showEditMovieDialog(currentMovie);
            });

            btnDelete = new JButton("Xóa");
            btnDelete.putClientProperty(FlatClientProperties.STYLE, "arc:5;border:2,8,2,8");
            btnDelete.setIcon(new FlatSVGIcon("raven/modal/demo/icons/delete.svg", 0.35f));
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                deleteMovie(currentMovie);
            });

            panel.add(btnEdit);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentMovie = (Movie) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentMovie;
        }
    }
}
