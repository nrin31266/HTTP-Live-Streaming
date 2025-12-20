package raven.modal.demo.forms;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;
import raven.modal.demo.system.Form;
import raven.modal.demo.utils.SystemForm;

import javax.swing.*;
import java.awt.*;

@SystemForm(name = "Dashboard", description = "User dashboard for movie & HLS streaming desktop app")
public class FormDashboard extends Form {

    private JPanel panelLayout;

    public FormDashboard() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill,insets 0", "[fill]", "[fill]"));
        createPanelLayout();

        // Nội dung tĩnh
        panelLayout.add(createHero(), "growx");
        panelLayout.add(createQuickActions(), "growx");
        panelLayout.add(createContinueWatching(), "growx");
        panelLayout.add(createTrending(), "growx");
        panelLayout.add(createLiveChannels(), "growx");
    }

    @Override
    public void formInit() {
        // tạm thời tĩnh, sau này gọi API ở đây
    }

    @Override
    public void formRefresh() {
        // tạm thời không làm gì
    }

    private void createPanelLayout() {
        panelLayout = new JPanel(new MigLayout("wrap,fillx,insets 14,gap 14", "[fill]", "[]"));

        JScrollPane scrollPane = new JScrollPane(panelLayout);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        // style scrollbar giống phong cách bạn đang dùng
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "" +
                "width:5;" +
                "trackArc:$ScrollBar.thumbArc;" +
                "trackInsets:0,0,0,0;" +
                "thumbInsets:0,0,0,0;");

        add(scrollPane);
    }

    // ---------------- UI SECTIONS ----------------

    private JComponent createHero() {
        JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 16", "[fill][grow 0]", "[]8[]8[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:18;" +
                "[light]background:tint($Panel.background,8%);" +
                "[dark]background:tint($Panel.background,4%);");

        JLabel title = new JLabel("Chào mừng bạn đến với HLS Streaming");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +6;");

        JLabel desc = new JLabel("<html>Xem phim theo yêu cầu và kênh trực tiếp. " +
                "Hỗ trợ HTTP Live Streaming (m3u8) – tối ưu cho mạng yếu.</html>");
        desc.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        JPanel stats = new JPanel(new MigLayout("insets 0,wrap,fillx", "[fill]", "[]6[]6[]"));
        stats.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        stats.add(createPill("🔴 3 kênh LIVE đang phát", true));
        stats.add(createPill("🎬 12 phim mới cập nhật tuần này", false));
        stats.add(createPill("📶 Chất lượng: Auto / 1080p / 720p / 480p", false));

        JButton btnOpenPlaylist = new JButton("Mở playlist (.m3u8)");
        btnOpenPlaylist.putClientProperty(FlatClientProperties.STYLE, "arc:12;margin:6,12,6,12;");
        btnOpenPlaylist.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Demo: Sau này bạn gắn JFileChooser để chọn file m3u8 hoặc URL.",
                    "Open Playlist", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnResume = new JButton("Tiếp tục xem");
        btnResume.putClientProperty(FlatClientProperties.STYLE, "arc:12;margin:6,12,6,12;");

        JPanel actions = new JPanel(new MigLayout("insets 0", "[]10[]", "[]"));
        actions.putClientProperty(FlatClientProperties.STYLE, "background:null;");
        actions.add(btnResume);
        actions.add(btnOpenPlaylist);

        panel.add(title, "span");
        panel.add(desc, "span");
        panel.add(actions, "span");

        panel.add(stats, "span,growx");

        // icon minh hoạ (nếu bạn có svg, còn không thì bỏ)
        JLabel icon = new JLabel(new FlatSVGIcon("raven/modal/demo/icons/dashboard/income.svg", 1.2f));
        icon.putClientProperty(FlatClientProperties.STYLE, "foreground:$Component.accentColor;");
        panel.add(icon, "pos 1al 1al"); // góc phải dưới nhẹ

        return panel;
    }

    private JComponent createQuickActions() {
        JPanel panel = createSection("Tác vụ nhanh", "Một vài thao tác phổ biến");

        JPanel grid = new JPanel(new MigLayout("fillx,insets 0,gap 12", "[fill][fill][fill]", "[]"));
        grid.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        grid.add(createActionCard("Tìm kiếm", "Tìm phim theo tên, thể loại", "raven/modal/demo/icons/search.svg"));
        grid.add(createActionCard("Thư viện", "Phim đã tải / danh sách xem sau", "raven/modal/demo/icons/dashboard/customer.svg"));
        grid.add(createActionCard("Cài đặt phát", "Chất lượng, buffer, phụ đề", "raven/modal/demo/icons/setting.svg"));

        panel.add(grid, "growx");
        return panel;
    }

    private JComponent createContinueWatching() {
        JPanel panel = createSection("Tiếp tục xem", "Các nội dung bạn đang xem dở");

        JPanel list = new JPanel(new MigLayout("wrap,fillx,insets 0,gap 10", "[fill]", "[]"));
        list.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        list.add(createMediaRow("Arcane S01E03", "Đã xem 18:24 / 42:10  •  720p  •  Vietsub", 44), "growx");
        list.add(createMediaRow("The Bear S02E01", "Đã xem 05:12 / 30:05  •  Auto  •  Engsub", 17), "growx");
        list.add(createMediaRow("One Piece (Live) EP06", "Đã xem 36:40 / 52:00  •  1080p", 70), "growx");

        panel.add(list, "growx");
        return panel;
    }

    private JComponent createTrending() {
        JPanel panel = createSection("Đang thịnh hành", "Top nội dung được xem nhiều");

        JPanel grid = new JPanel(new MigLayout("fillx,insets 0,gap 12", "[fill][fill][fill][fill]", "[]"));
        grid.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        grid.add(createPosterCard("Dune: Part Two", "Sci-Fi • 2024", "8.8", "raven/modal/demo/icons/dashboard/profit.svg"));
        grid.add(createPosterCard("Oppenheimer", "Drama • 2023", "8.7", "raven/modal/demo/icons/dashboard/profit.svg"));
        grid.add(createPosterCard("The Boys", "Series • S04", "8.5", "raven/modal/demo/icons/dashboard/profit.svg"));
        grid.add(createPosterCard("Jujutsu Kaisen", "Anime • S02", "8.6", "raven/modal/demo/icons/dashboard/profit.svg"));

        panel.add(grid, "growx");
        return panel;
    }

    private JComponent createLiveChannels() {
        JPanel panel = createSection("Kênh trực tiếp (LIVE)", "Demo danh sách kênh HLS / IPTV");

        JPanel list = new JPanel(new MigLayout("wrap,fillx,insets 0,gap 10", "[fill]", "[]"));
        list.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        list.add(createLiveRow("VTV1 HD", "Tin tức • 1080p • HLS", "ON AIR"), "growx");
        list.add(createLiveRow("VTV3", "Giải trí • 720p • HLS", "ON AIR"), "growx");
        list.add(createLiveRow("HTV7", "Tổng hợp • Auto • HLS", "ON AIR"), "growx");
        list.add(createLiveRow("Anime 24/7", "Anime • 720p • HLS", "OFFLINE"), "growx");

        panel.add(list, "growx");
        return panel;
    }

    // ---------------- REUSABLE COMPONENTS ----------------

    private JPanel createSection(String title, String subtitle) {
        JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 16", "[fill]", "[]6[]12[]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:18;" +
                "[light]background:tint($Panel.background,6%);" +
                "[dark]background:tint($Panel.background,3%);");

        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2;");

        JLabel lbSub = new JLabel(subtitle);
        lbSub.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        panel.add(lbTitle);
        panel.add(lbSub);

        return panel;
    }

    private JComponent createPill(String text, boolean accent) {
        JLabel lb = new JLabel(text);
        lb.putClientProperty(FlatClientProperties.STYLE,
                "arc:999;border:6,10,6,10;" +
                        (accent ? "background:$Component.accentColor;foreground:$Panel.background;" :
                                "[light]background:tint($Panel.background,12%);" +
                                        "[dark]background:tint($Panel.background,8%);")
        );
        lb.setOpaque(true);
        return lb;
    }

    private JComponent createActionCard(String title, String desc, String svgIconPath) {
        JPanel card = new JPanel(new MigLayout("wrap,fillx,insets 14", "[]", "[]6[]"));
        card.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:16;" +
                "[light]background:tint($Panel.background,10%);" +
                "[dark]background:tint($Panel.background,6%);");

        JLabel icon = new JLabel(new FlatSVGIcon(svgIconPath, 0.85f));
        JLabel lbTitle = new JLabel(title);
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold;");

        JLabel lbDesc = new JLabel(desc);
        lbDesc.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        card.add(icon);
        card.add(lbTitle);
        card.add(lbDesc);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(FormDashboard.this,
                        "Demo: Click \"" + title + "\"",
                        "Action", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return card;
    }

    private JComponent createMediaRow(String name, String meta, int progressPercent) {
        JPanel row = new JPanel(new MigLayout("fillx,insets 12", "[]12[fill]12[grow 0]", "[]6[]"));
        row.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:16;" +
                "[light]background:tint($Panel.background,10%);" +
                "[dark]background:tint($Panel.background,6%);");

        JLabel icon = new JLabel(new FlatSVGIcon("raven/modal/demo/icons/dashboard/customer.svg", 0.7f));

        JLabel lbName = new JLabel(name);
        lbName.putClientProperty(FlatClientProperties.STYLE, "font:bold;");

        JLabel lbMeta = new JLabel(meta);
        lbMeta.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(progressPercent);
        bar.putClientProperty(FlatClientProperties.STYLE, "arc:999;");
        bar.setPreferredSize(new java.awt.Dimension(0, 8));

        JButton btnPlay = new JButton("Phát");
        btnPlay.putClientProperty(FlatClientProperties.STYLE, "arc:12;margin:6,12,6,12;");
        btnPlay.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Demo: Play \"" + name + "\"",
                        "Play", JOptionPane.INFORMATION_MESSAGE)
        );

        JPanel center = new JPanel(new MigLayout("wrap,fillx,insets 0", "[fill]", "[]6[]6[]"));
        center.putClientProperty(FlatClientProperties.STYLE, "background:null;");
        center.add(lbName);
        center.add(lbMeta);
        center.add(bar, "growx");

        row.add(icon);
        row.add(center, "growx");
        row.add(btnPlay, "top");

        return row;
    }

    private JComponent createPosterCard(String name, String meta, String rating, String svgIconPath) {
        JPanel card = new JPanel(new MigLayout("wrap,fillx,insets 12", "[fill]", "[]10[]8[]"));
        card.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:16;" +
                "[light]background:tint($Panel.background,10%);" +
                "[dark]background:tint($Panel.background,6%);");

        JLabel poster = new JLabel(new FlatSVGIcon(svgIconPath, 1.1f));
        poster.putClientProperty(FlatClientProperties.STYLE, "foreground:$Component.accentColor;");

        JLabel lbName = new JLabel(name);
        lbName.putClientProperty(FlatClientProperties.STYLE, "font:bold;");

        JLabel lbMeta = new JLabel(meta);
        lbMeta.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        JLabel lbRating = new JLabel("★ " + rating);
        lbRating.putClientProperty(FlatClientProperties.STYLE, "font:bold;");

        card.add(poster, "al center");
        card.add(lbName);
        card.add(lbMeta);
        card.add(lbRating);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(FormDashboard.this,
                        "Demo: Open detail \"" + name + "\"",
                        "Detail", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return card;
    }

    private JComponent createLiveRow(String name, String meta, String status) {
        JPanel row = new JPanel(new MigLayout("fillx,insets 12", "[]12[fill]12[grow 0]", "[]6[]"));
        row.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:16;" +
                "[light]background:tint($Panel.background,10%);" +
                "[dark]background:tint($Panel.background,6%);");

        JLabel icon = new JLabel(new FlatSVGIcon("raven/modal/demo/icons/dashboard/income.svg", 0.7f));
        icon.putClientProperty(FlatClientProperties.STYLE, "" +
                (status.equals("ON AIR") ? "foreground:#ef4444;" : "foreground:$Label.disabledForeground;"));

        JLabel lbName = new JLabel(name);
        lbName.putClientProperty(FlatClientProperties.STYLE, "font:bold;");

        JLabel lbMeta = new JLabel(meta);
        lbMeta.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        JLabel lbStatus = new JLabel(status);
        lbStatus.setOpaque(true);
        lbStatus.putClientProperty(FlatClientProperties.STYLE,
                "arc:999;border:4,10,4,10;" +
                        (status.equals("ON AIR")
                                ? "background:#ef4444;foreground:#ffffff;"
                                : "[light]background:tint($Panel.background,12%);" +
                                "[dark]background:tint($Panel.background,8%);" +
                                "foreground:$Label.disabledForeground;")
        );

        JButton btnWatch = new JButton("Xem");
        btnWatch.putClientProperty(FlatClientProperties.STYLE, "arc:12;margin:6,12,6,12;");
        btnWatch.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Demo: Watch channel \"" + name + "\"",
                        "Live", JOptionPane.INFORMATION_MESSAGE)
        );

        JPanel center = new JPanel(new MigLayout("wrap,fillx,insets 0", "[fill]", "[]6[]"));
        center.putClientProperty(FlatClientProperties.STYLE, "background:null;");
        center.add(lbName);
        center.add(lbMeta);

        row.add(icon);
        row.add(center, "growx");
        row.add(lbStatus, "aligny top");
        row.add(btnWatch, "aligny top");

        return row;
    }
}
