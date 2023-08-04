package Verification.Initializer;

import Verification.API.GET;
import Verification.API.POST;

public class Initializer {

	public static void initialize() {
		GET.run();
		POST.run();
	}
}
