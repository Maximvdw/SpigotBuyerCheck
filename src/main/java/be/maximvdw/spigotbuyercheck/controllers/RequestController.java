package be.maximvdw.spigotbuyercheck.controllers;

import be.maximvdw.spigotbuyercheck.SpigotSiteServer;
import be.maximvdw.spigotbuyercheck.service.ServiceLocator;
import be.maximvdw.spigotsite.api.resource.Buyer;
import be.maximvdw.spigotsite.api.resource.PremiumResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * RequestController
 * <p>
 * Created by maxim on 19-Dec-16.
 */
@Controller
@RequestMapping()
public class RequestController {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss z");

    @RequestMapping(value = "{userId}", method = RequestMethod.GET)
    public ModelAndView buyerPage(@PathVariable( value = "userId") int userId) {
        ModelAndView result = new ModelAndView("buyer");
        SpigotSiteServer spigotSiteServer = ServiceLocator.getSpigotSiteServer();
        result.addObject("userId", userId);

        Map<PremiumResource, Buyer> plugins = spigotSiteServer.isInBuyersForPlugins(userId);
        if (plugins.isEmpty()) {
            result.addObject("error", 2);
            return result;
        }

        result.addObject("plugins", plugins);
        result.addObject("success", true);
        result.addObject("username", plugins.values().iterator().next().getUsername());

        // Check if ready
        if (spigotSiteServer.hasError()) {
            // Unable to connect to spigot
            result.addObject("error", 5);
            return result;
        }
        if (!spigotSiteServer.isReady()) {
            // Still starting
            result.addObject("error", 4);
            return result;
        }
        return result;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView loginPage() {
        ModelAndView result = new ModelAndView("index");
        SpigotSiteServer spigotSiteServer = ServiceLocator.getSpigotSiteServer();

        // Last sync
        result.addObject("lastSync", spigotSiteServer.getLastSync() / 1000);
        result.addObject("lastSyncFormatted", simpleDateFormat.format(new Date(spigotSiteServer.getLastSync() / 1000)));

        // Check if ready
        if (spigotSiteServer.hasError()) {
            // Unable to connect to spigot
            result.addObject("error", 5);
            return result;
        }
        if (!spigotSiteServer.isReady()) {
            // Still starting
            result.addObject("error", 4);
            return result;
        }
        return result;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ModelAndView buyerCheckRequest(@RequestParam(value = "username", defaultValue = "") String username) {
        ModelAndView result = new ModelAndView("index");
        SpigotSiteServer spigotSiteServer = ServiceLocator.getSpigotSiteServer();

        // Last sync
        result.addObject("lastSync", spigotSiteServer.getLastSync() / 1000);
        result.addObject("lastSyncFormatted", simpleDateFormat.format(new Date(spigotSiteServer.getLastSync())));

        // Check if ready
        if (spigotSiteServer.hasError()) {
            // Unable to connect to spigot
            result.addObject("error", 5);
            return result;
        }
        if (!spigotSiteServer.isReady()) {
            // Still starting
            result.addObject("error", 4);
            return result;
        }

        if (username.equals("")) {
            result.addObject("error", 3);
            return result;
        }
        result.addObject("inputUsername", username);

        try {
            if (!spigotSiteServer.isValidUser(username)) {
                result.addObject("error", 1);
                return result;
            }
        } catch (Exception ex) {
            // Connection error

        }

        result.addObject("username", username);
        Map<PremiumResource, Buyer> plugins = spigotSiteServer.isInBuyersForPlugins(username);
        if (plugins.isEmpty()) {
            result.addObject("error", 2);
            return result;
        }


        result.addObject("userId", plugins.values().iterator().next().getUserId());

        result.addObject("success", true);
        result.addObject("inputUsername", "");
        result.addObject("plugins", plugins);

        return result;
    }
}
