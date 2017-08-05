package be.maximvdw.spigotbuyercheck.service;

import be.maximvdw.spigotbuyercheck.SpigotSiteServer;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Maxim Van de Wynckel
 * @date 12-May-16
 */
public class ServiceLocator {
    public static String SPIGOTSITE_SERVER = "java:module/SpigotSiteServerBean";

    private static SpigotSiteServer spigotSiteServer;


    /**
     * Get the spigot site server
     *
     * @return spigot site server
     */
    public static SpigotSiteServer getSpigotSiteServer() {
        if (spigotSiteServer == null) {
            spigotSiteServer = (SpigotSiteServer) ServiceLocator.doLookup(ServiceLocator.SPIGOTSITE_SERVER);
        }
        return spigotSiteServer;
    }

    public static Object doLookup(String name){
        try {
            return InitialContext.doLookup(name);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
