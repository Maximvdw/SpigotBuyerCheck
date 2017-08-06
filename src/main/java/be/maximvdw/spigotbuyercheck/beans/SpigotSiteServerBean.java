package be.maximvdw.spigotbuyercheck.beans;

import be.maximvdw.spigotbuyercheck.SpigotSiteServer;
import be.maximvdw.spigotbuyercheck.config.Configuration;
import be.maximvdw.spigotbuyercheck.schedulers.SchedulerManager;
import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSite;
import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.PremiumResource;
import be.maximvdw.spigotsite.api.resource.Resource;
import be.maximvdw.spigotsite.api.resource.ResourceManager;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;
import be.maximvdw.spigotsite.api.user.exceptions.TwoFactorAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.ArrayList;
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
    private Map<String, ArrayList<String>> buyers = new ConcurrentHashMap<>();

    private User pluginAuthor = null;
    private boolean error = false;
    private BuyerFetchTask buyerFetchTask = new BuyerFetchTask();

    private List<PremiumResource> resources = new ArrayList<>();

    @PostConstruct
    public void init() throws ConnectionFailedException {
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
        List<Resource> allResources = resourceManager.getResourcesByUser(pluginAuthor);
        for (Resource resource : allResources) {
            if (resource instanceof PremiumResource) {
                resources.add((PremiumResource) resource);
            }
        }
        SchedulerManager.createAsyncTask(buyerFetchTask, 30, TimeUnit.MINUTES);
    }

    @Override
    public boolean isReady() {
        return initialized;
    }

    @Override
    public boolean isValidUser(String username) {
        User u = SpigotSite.getAPI().getUserManager().getUserByName(username);
        return u != null;
    }

    @Override
    public boolean isInBuyers(String pluginName, String username) {
        ArrayList<String> usernames = buyers.get(pluginName);
        if (usernames.contains(username.toLowerCase())) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> isInBuyersForPlugins(String username) {
        List<String> plugins = new ArrayList<>();
        for (Resource resource : resources) {
            ArrayList<String> usernames = buyers.get(resource.getResourceName());
            if (usernames.contains(username.toLowerCase())) {
                plugins.add(resource.getResourceName());
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

    public class BuyerFetchTask implements Runnable {

        @Override
        public void run() {
            try {
                ResourceManager resourceManager = SpigotSite.getAPI().getResourceManager();
                logger.info("Refreshing buyers list ...");
                for (PremiumResource resource : resources) {
                    logger.info("Loading buyers from: " + resource.getResourceName() + "...");
                    try {
                        List<User> users = resourceManager.getPremiumResourceBuyers(resource, pluginAuthor);
                        ArrayList<String> usernames = new ArrayList<String>();
                        for (User u : users) {
                            usernames.add(u.getUsername().toLowerCase());
                        }
                        buyers.put(resource.getResourceName(), usernames);
                    } catch (ConnectionFailedException e) {
                        logger.error("Unable to load buyers ...", e);
                    }
                }
                initialized = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}