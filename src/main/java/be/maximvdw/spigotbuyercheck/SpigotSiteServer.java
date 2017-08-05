package be.maximvdw.spigotbuyercheck;

import java.util.List;

/**
 * Created by maxim on 19-Dec-16.
 */
public interface SpigotSiteServer {

    boolean isReady();

    boolean hasError();

    boolean isValidUser(String username);

    boolean isInBuyers(String pluginName, String username);

    List<String> isInBuyersForPlugins(String username);
}
