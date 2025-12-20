package raven.modal.demo.auth;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import raven.modal.component.DropShadowBorder;
import raven.modal.demo.api.AuthApi;
import raven.modal.demo.dto.response.ApiResponse;
import raven.modal.demo.dto.response.AuthResponse;
import raven.modal.demo.component.LabelButton;
import raven.modal.demo.menu.MyDrawerBuilder;
import raven.modal.demo.model.ModelUser;
import raven.modal.demo.system.Form;
import raven.modal.demo.system.FormManager;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Login extends Form {

    private JLabel lbError;   // 👈 label hiển thị lỗi

    public Login() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("al center center"));
        createLogin();
    }

    private void createLogin() {
        JPanel panelLogin = new JPanel(new BorderLayout()) {
            @Override
            public void updateUI() {
                super.updateUI();
                applyShadowBorder(this);
            }
        };
        panelLogin.setOpaque(false);
        applyShadowBorder(panelLogin);

        JPanel loginContent = new JPanel(new MigLayout("fillx,wrap,insets 35 35 25 35", "[fill,300]"));

        JLabel lbTitle = new JLabel("Chào mừng trở lại!");
        JLabel lbDescription = new JLabel("Hãy đăng nhập vào tài khoản của bạn");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +12;");

        loginContent.add(lbTitle);
        loginContent.add(lbDescription);

        JTextField txtEmail = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JButton cmdLogin = new JButton("Đăng nhập") {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };

        // style
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập email của bạn");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu của bạn");

        panelLogin.putClientProperty(FlatClientProperties.STYLE,
                "[dark]background:tint($Panel.background,1%);");

        loginContent.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        txtEmail.putClientProperty(FlatClientProperties.STYLE,
                "margin:4,10,4,10;arc:12;");
        txtPassword.putClientProperty(FlatClientProperties.STYLE,
                "margin:4,10,4,10;arc:12;showRevealButton:true;");

        cmdLogin.putClientProperty(FlatClientProperties.STYLE,
                "margin:4,10,4,10;arc:12;");

        // label error (dòng đỏ nhỏ dưới nút)
        lbError = new JLabel(" ");
        lbError.putClientProperty(FlatClientProperties.STYLE,
                "foreground:#f87171;"); // đỏ nhạt

        loginContent.add(new JLabel("Email"), "gapy 25");
        loginContent.add(txtEmail);

        loginContent.add(new JLabel("Password"), "gapy 10");
        loginContent.add(txtPassword);

        loginContent.add(cmdLogin, "gapy 20");
        loginContent.add(lbError);   // 👈 thêm dòng error ở đây

        loginContent.add(showRegister(), "gapy 10");
        loginContent.add(createInfo());

        panelLogin.add(loginContent);
        add(panelLogin);

        // event login
        cmdLogin.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            String password = String.valueOf(txtPassword.getPassword());
            // validate đơn giản
            if (email.isEmpty() || password.isEmpty()) {
                lbError.setText("Vui lòng nhập email và mật khẩu");
                return;
            }

            lbError.setText("Đang đăng nhập...");
            cmdLogin.setEnabled(false);

            // gọi API trên background để không block UI
            new SwingWorker<ApiResponse<AuthResponse>, Void>() {
                @Override
                protected ApiResponse<AuthResponse> doInBackground() {
                    return AuthApi.login(email, password);
                }

                @Override
                protected void done() {
                    cmdLogin.setEnabled(true);
                    try {
                        ApiResponse<AuthResponse> res = get();

                        if (!res.isSuccess() || res.getResult() == null) {
                            // hiện lỗi từ server
                            String msg = res.getMessage() != null ? res.getMessage() : "Đăng nhập thất bại";
                            lbError.setText(msg);
                            return;
                        }

                        AuthResponse auth = res.getResult();

                        // map AuthResponse -> ModelUser
                        ModelUser user = mapToModelUser(auth);
                        MyDrawerBuilder.getInstance().setUser(user);

                        lbError.setText(" "); // clear lỗi
                        System.out.println(user.getRole());
                        if (user.getRole() == ModelUser.Role.ADMIN) {

                            FormManager.loginAdmin();

                        } else {
                            FormManager.login();
                        }

                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        lbError.setText("Lỗi: " + ex.getCause().getMessage());
                    }
                }
            }.execute();
        });
    }

    private ModelUser mapToModelUser(AuthResponse auth) {
        // auth.getRoles(): Set<String> kiểu ["ROLE_ADMIN","ROLE_USER",...]
        Set<String> roles = auth.getRoles();
        ModelUser.Role role = ModelUser.Role.USER;
        if (roles != null && roles.stream().anyMatch(r -> r.contains("ADMIN"))) {
            role = ModelUser.Role.ADMIN;
        }

        return new ModelUser(
                auth.getFullName(),
                auth.getEmail(),
                role
        );
    }

    private JPanel showRegister() {
        JPanel panel = new JPanel(new MigLayout("wrap,al center", "[center]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        JLabel lbQuestion = new JLabel("Bạn chưa có tài khoản?");
        LabelButton lbRegister = new LabelButton("Đăng ký ngay");
        panel.add(lbQuestion, "split 2");
        panel.add(lbRegister);

        lbRegister.addOnClick(e -> FormManager.showRegister());
        return panel;
    }

    private JPanel createInfo() {
        JPanel panelInfo = new JPanel(new MigLayout("wrap,al center", "[center]"));
        panelInfo.putClientProperty(FlatClientProperties.STYLE, "background:null;");

        panelInfo.add(new JLabel("Liên hệ tại"), "split 2");
        LabelButton lbLink = new LabelButton("rinnv.23it@vku.udn.vn");
        panelInfo.add(lbLink);

        lbLink.addOnClick(e -> {
            // mở mailto: nếu thích
        });
        return panelInfo;
    }

    private void applyShadowBorder(JPanel panel) {
        if (panel != null) {
            panel.setBorder(new DropShadowBorder(new Insets(5, 8, 12, 8), 1, 25));
        }
    }
}
