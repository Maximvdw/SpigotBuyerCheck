package be.maximvdw.spigotbuyercheck.controllers;

import be.maximvdw.spigotbuyercheck.SpigotSiteServer;
import be.maximvdw.spigotbuyercheck.service.ServiceLocator;
import be.maximvdw.spigotsite.api.SpigotSite;
import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.Buyer;
import be.maximvdw.spigotsite.api.resource.PremiumResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.List;
import java.util.Map;

/**
 * APIController
 */
@Controller
@RequestMapping("/api")
public class APIController {
    /**
     * Provide autocomplete
     *
     * @param username username or match
     * @return JSON response with list of possible usernames
     */
    @RequestMapping(value = "/user/fromName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    ResponseEntity<String> fromName(
            @RequestParam("q") String username) throws ConnectionFailedException {
        List<String> matches = SpigotSite.getAPI().getUserManager().getUsernamesByName(username);
        JsonArrayBuilder matchValues = Json.createArrayBuilder();
        for (String match : matches) {
            matchValues.add(match);
        }
        JsonObject result = Json.createObjectBuilder()
                .add("success", 1)
                .add("matches", matchValues.build()).build();
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }


    @RequestMapping(value = "/checkbuyer/{pluginName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    ResponseEntity<String> checkBuyer(
            @PathVariable("pluginName") String plugin,
            @RequestParam(value = "user_id", required = false) Integer userId,
            @RequestParam(value = "username", required = false) String username) throws ConnectionFailedException {
        SpigotSiteServer spigotSiteServer = ServiceLocator.getSpigotSiteServer();

        boolean buyer = false;
        if (username != null) {
            if (spigotSiteServer.isInBuyers(plugin, username)) {
                buyer = true;
            }
        } else {
            if (spigotSiteServer.isInBuyers(plugin, userId)) {
                buyer = true;
            }
        }
        JsonObject result = Json.createObjectBuilder()
                .add("bought", buyer).build();
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/checkbuyer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    ResponseEntity<String> boughtPlugins(
            @RequestParam(value = "user_id", required = false) Integer userId,
            @RequestParam(value = "username", required = false) String username) throws ConnectionFailedException {
        SpigotSiteServer spigotSiteServer = ServiceLocator.getSpigotSiteServer();
        JsonArrayBuilder pluginsArray = null;
        if (username != null) {
            Map<PremiumResource, Buyer> plugins = spigotSiteServer.isInBuyersForPlugins(username);
            pluginsArray = Json.createArrayBuilder();
            for (Map.Entry<PremiumResource, Buyer> plugin : plugins.entrySet()) {
                pluginsArray.add(Json.createObjectBuilder().add("name", plugin.getKey().getResourceName()).add("id", plugin.getKey().getResourceId()));
            }
        } else {
            Map<PremiumResource, Buyer> plugins = spigotSiteServer.isInBuyersForPlugins(userId);
            pluginsArray = Json.createArrayBuilder();
            for (Map.Entry<PremiumResource, Buyer> plugin : plugins.entrySet()) {
                pluginsArray.add(Json.createObjectBuilder().add("name", plugin.getKey().getResourceName()).add("id", plugin.getKey().getResourceId()));
            }
        }
        JsonObject result = Json.createObjectBuilder()
                .add("bought", pluginsArray.build()
                ).build();
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }
}
