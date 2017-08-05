package be.maximvdw.spigotbuyercheck.controllers;

import be.maximvdw.spigotbuyercheck.SpigotSiteServer;
import be.maximvdw.spigotbuyercheck.service.ServiceLocator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * RequestController
 * <p>
 * Created by maxim on 19-Dec-16.
 */
@Controller
@RequestMapping()
public class RequestController {
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView loginPage() {
        ModelAndView result = new ModelAndView("index");
        // Check if ready
        if (ServiceLocator.getSpigotSiteServer().hasError()) {
            result.addObject("error", 9);
            return result;
        }
        if (!ServiceLocator.getSpigotSiteServer().isReady()) {
            result.addObject("error", 8);
            return result;
        }
        return result;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ModelAndView buyerCheckRequest(@RequestParam(value = "username", defaultValue = "") String username) {
        ModelAndView result = new ModelAndView("index");
        SpigotSiteServer spigotSiteServer = ServiceLocator.getSpigotSiteServer();

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

        if (!spigotSiteServer.isValidUser(username)){
            result.addObject("error", 1);
            return result;
        }

        List<String> plugins = spigotSiteServer.isInBuyersForPlugins(username);
        if (plugins.isEmpty()) {
            result.addObject("error", 2);
            return result;
        }

        result.addObject("success", true);
        result.addObject("username", username);
        result.addObject("inputUsername", "");
        result.addObject("plugins", plugins);

        return result;
    }
}
