package TicmintToken;

import TicmintToken.API.POST;
import TicmintToken.API.GET;

public class Initializer {

    public static void init() {
        GET.run();
        POST.run();
    }
}
