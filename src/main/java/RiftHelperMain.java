import controller.RiftHelperMainController;
import model.LCUAuth;
import model.SSLBypass;
import view.RiftHelperMainView;

public class RiftHelperMain {
    private RiftHelperMainView riftHelperMainView = new RiftHelperMainView();
    private RiftHelperMainController riftHelperMainController;

    public RiftHelperMain() {
        try {
            SSLBypass.disableSSLVerification();

            if (!LCUAuth.getLCUAuth()) {
                System.out.println("League Client not found.");
                return;
            }

            System.out.println("Port: " + LCUAuth.port);
            System.out.println("Auth Token: " + LCUAuth.token);

            riftHelperMainController = new RiftHelperMainController(riftHelperMainView);
            riftHelperMainView.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new RiftHelperMain();
    }
}
