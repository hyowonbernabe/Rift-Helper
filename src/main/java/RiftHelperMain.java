import controller.RiftHelperController;
import model.LCUAuth;
import model.SSLBypass;
import view.RiftHelperView;

public class RiftHelperMain {
    private RiftHelperView riftHelperView = new RiftHelperView();
    private RiftHelperController riftHelperController;

    public RiftHelperMain() {
        try {
            SSLBypass.disableSSLVerification();

            if (!LCUAuth.getLCUAuth()) {
                System.out.println("League Client not found.");
                return;
            }

            System.out.println("Port: " + LCUAuth.port);
            System.out.println("Auth Token: " + LCUAuth.token);

            riftHelperController = new RiftHelperController(riftHelperView);
            riftHelperView.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new RiftHelperMain();
    }
}
