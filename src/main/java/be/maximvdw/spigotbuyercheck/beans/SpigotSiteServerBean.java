package be.maximvdw.spigotbuyercheck.beans;

import be.maximvdw.spigotbuyercheck.SpigotSiteServer;
import be.maximvdw.spigotbuyercheck.config.Configuration;
import be.maximvdw.spigotbuyercheck.schedulers.SchedulerManager;
import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSite;
import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.Buyer;
import be.maximvdw.spigotsite.api.resource.PremiumResource;
import be.maximvdw.spigotsite.api.resource.Resource;
import be.maximvdw.spigotsite.api.resource.ResourceManager;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;
import be.maximvdw.spigotsite.api.user.exceptions.TwoFactorAuthenticationException;
import be.maximvdw.spigotsite.resource.SpigotPremiumResource;
import be.maximvdw.spigotsite.user.SpigotUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * SpigotSiteServerBean
 * <p>
 * Created by maxim on 19-Dec-16.
 */
@Startup
@Remote(SpigotSiteServer.class)
@Singleton(mappedName = "SpigotSiteServer")
public class SpigotSiteServerBean implements SpigotSiteServer {
    // Logging
    private final Logger logger = LoggerFactory.getLogger(SpigotSiteServerBean.class);
    private boolean initialized = false;

    private User pluginAuthor = null;
    private boolean error = false;
    private BuyerFetchTask buyerFetchTask = new BuyerFetchTask();
    private long lastSync = 0L;
    private boolean syncing = false;

    private Map<String, PremiumResource> resources = new ConcurrentHashMap<>();

    @PostConstruct
    public void init()  {
        new SpigotSiteCore();
        new Configuration("SpigotBuyerCheck", 1); // Version 1

        String username = Configuration.getString("username");
        String password = Configuration.getString("password");
        String totpSecret = Configuration.getString("2fakey");

        if (username.equals("") || password.equals("")) {
            logger.error("Not configured yet!");
            setError(true);
            return;
        }

        logger.info("Logging in " + username + " ...");
        try {
            pluginAuthor = SpigotSite.getAPI().getUserManager()
                    .authenticate(username, password, totpSecret);
        } catch (InvalidCredentialsException e) {
            logger.info("Unable to log in! Wrong credentials!");
            setError(true);
            return;
        } catch (TwoFactorAuthenticationException e) {
            logger.info("Unable to log in! Two factor authentication failed!");
            setError(true);
            return;
        } catch (Exception e) {
            logger.error("Something went wrong!", e);
            setError(true);
            return;
        }
        ResourceManager resourceManager = SpigotSite.getAPI().getResourceManager();
        List<Resource> allResources = null;
        try {
            allResources = resourceManager.getResourcesByUser(pluginAuthor);
        } catch (ConnectionFailedException e) {
            e.printStackTrace();
        }
        for (Resource resource : allResources) {
            if (resource instanceof PremiumResource) {
                resources.put(resource.getResourceName().toLowerCase(), (PremiumResource) resource);
            }
        }
        SchedulerManager.createAsyncTask(buyerFetchTask, 30, TimeUnit.MINUTES);
    }

    @Override
    public boolean isReady() {
        return initialized;
    }

    @Override
    public boolean isValidUser(String username) throws ConnectionFailedException {
        return SpigotSite.getAPI().getUserManager().getUserByName(username) != null;
    }

    @Override
    public boolean isInBuyers(String pluginName, String username) {
        PremiumResource resource = resources.get(pluginName.toLowerCase());
        return resource.isBuyer(new SpigotUser(username));
    }

    @Override
    public boolean isInBuyers(String pluginName, int userId) {
        PremiumResource resource = resources.get(pluginName.toLowerCase());
        return resource.isBuyer(new SpigotUser(userId));
    }

    @Override
    public Map<PremiumResource, Buyer> isInBuyersForPlugins(String username) {
        Map<PremiumResource, Buyer> plugins = new HashMap<>();
        for (PremiumResource resource : resources.values()) {
            Buyer buyer = resource.getBuyerByName(username);
            if (buyer != null) {
                plugins.put(resource, buyer);
            }
        }
        return plugins;
    }

    @Override
    public Map<PremiumResource, Buyer> isInBuyersForPlugins(int userId) {
        Map<PremiumResource, Buyer> plugins = new HashMap<>();
        for (PremiumResource resource : resources.values()) {
            Buyer buyer = resource.getBuyerByUserId(userId);
            if (buyer != null) {
                plugins.put(resource, buyer);
            }
        }
        return plugins;
    }

    public boolean hasError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    @Override
    public long getLastSync() {
        return lastSync;
    }

    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }

    public void setSyncing(boolean syncing) {
        this.syncing = syncing;
    }

    public class BuyerFetchTask implements Runnable {

        @Override
        public void run() {
            try {
                ResourceManager resourceManager = SpigotSite.getAPI().getResourceManager();
                logger.info("Refreshing buyers list ...");
                setSyncing(true);
                for (PremiumResource resource : resources.values()) {
                    logger.info("Loading buyers from: " + resource.getResourceName() + "...");
                    try {
                        List<Buyer> buyers = resourceManager.getPremiumResourceBuyers(resource, pluginAuthor);
                        ((SpigotPremiumResource) resource).setBuyers(buyers);
                        resources.put(resource.getResourceName().toLowerCase(), resource);
                    } catch (ConnectionFailedException e) {
                        logger.error("Unable to load buyers ...", e);
                    }
                }
                setSyncing(false);
                setLastSync(System.currentTimeMillis());
                initialized = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}