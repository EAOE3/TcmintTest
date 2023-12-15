package Wallet.Initializer;

import Database.DBM;
import Wallet.API.POST;
import Wallet.API.GET;
import Wallet.Wallet.Wallet;

public class Initializer {

	public static void initialize() {
		try { DBM.loadAllObjectsFromDatabase(Wallet.class); } catch (Exception e) { e.printStackTrace(); }

		GET.run();
		POST.run();
	}
}
