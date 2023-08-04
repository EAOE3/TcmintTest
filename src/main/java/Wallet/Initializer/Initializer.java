package Wallet.Initializer;

import REST_API.POST;
import Wallet.API.GET;
public class Initializer {

	public static void initialize() {
		GET.run();
		POST.run();
	}
}
