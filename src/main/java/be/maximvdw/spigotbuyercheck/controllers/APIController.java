package be.maximvdw.spigotbuyercheck.controllers;

import be.maximvdw.spigotsite.api.SpigotSite;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.List;

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
            @RequestParam("q") String username) {
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
}
