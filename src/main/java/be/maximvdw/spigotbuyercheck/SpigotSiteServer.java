package be.maximvdw.spigotbuyercheck;

import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.Buyer;
import be.maximvdw.spigotsite.api.resource.PremiumResource;

import java.util.List;
import java.util.Map;

/**
 * SpigotSiteServer
 * Created by maxim on 19-Dec-16.
 */
public interface SpigotSiteServer {

    boolean isReady();

    boolean hasError();

    boolean isValidUser(String username) throws ConnectionFailedException;

    boolean isInBuyers(String pluginName, String username);

    boolean isInBuyers(String pluginName, int userId);

    Map<PremiumResource, Buyer> isInBuyersForPlugins(String username);

    Map<PremiumResource, Buyer> isInBuyersForPlugins(int userId);

    long getLastSync();
}
