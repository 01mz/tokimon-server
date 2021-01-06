package ca.cmpt213.a5.controller;

import ca.cmpt213.a5.model.Tokimon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class TokimonController {
    private final AtomicInteger counter = new AtomicInteger();
    private final Map<Integer, Tokimon> tokimonMap = new HashMap<>();
    File tokimonJsonFile = new File("./data/tokimon.json");

    Gson gson = new Gson();

    //@RequestMapping(value = "/tokimon/all", method = RequestMethod.GET)
    @GetMapping("/api/tokimon/all")
    public List<Tokimon> getAllTokimon(HttpServletResponse response) {
        System.out.println("GET all Tokimon");
        response.setStatus(HttpServletResponse.SC_OK);
        return new ArrayList<>(tokimonMap.values());
    }

    @GetMapping("/api/tokimon/{id}")
    public Tokimon getTokimon(@PathVariable int id, HttpServletResponse response) {
        // Get Tokimon by id
        System.out.println("GET Tokimon " + id);
        if (!tokimonMap.containsKey(id)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // tokimon with id not found
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No existing tokimon with id: " + id);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        return tokimonMap.get(id);
    }

    @PostMapping("/api/tokimon/add")
    public void addTokimon(@RequestBody Tokimon tokimon, HttpServletResponse response) {
        // create new Tokimon and add to tokimonMap
        tokimon.setId(counter.incrementAndGet());
        tokimonMap.put(tokimon.getId(), tokimon);

        saveToTokimonJson();

        response.setStatus(HttpServletResponse.SC_CREATED);

        System.out.println("POST: Added new tokimon id: " + tokimon.getId());
        System.out.println(gson.toJson(tokimon));
    }

    @PostMapping("/api/tokimon/change/{id}")
    public void alterTokimon(@PathVariable int id, @RequestBody Tokimon alteredTokimon, HttpServletResponse response) {
        if (!tokimonMap.containsKey(id)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // tokimon with id not found
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No existing tokimon with id: " + id);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println("POST: Altering tokimon id: " + id);
        // alter tokimon by id
        Tokimon existingTokimon = tokimonMap.get(id);
        if (alteredTokimon != null) {
            if (alteredTokimon.getName() != null) {
                existingTokimon.setName(alteredTokimon.getName());
            }
            if (alteredTokimon.getWeight() != null) {
                existingTokimon.setWeight(alteredTokimon.getWeight());
            }
            if (alteredTokimon.getHeight() != null) {
                existingTokimon.setHeight(alteredTokimon.getHeight());
            }
            if (alteredTokimon.getAbility() != null) {
                existingTokimon.setAbility(alteredTokimon.getAbility());
            }
            if (alteredTokimon.getStrength() != null) {
                existingTokimon.setStrength(alteredTokimon.getStrength());
            }
            if (alteredTokimon.getColor() != null) {
                existingTokimon.setColor(alteredTokimon.getColor());
            }
        }

        saveToTokimonJson();
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @DeleteMapping("/api/tokimon/{id}")
    public void deleteTokimon(@PathVariable int id, HttpServletResponse response) {
        if (!tokimonMap.containsKey(id)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // tokimon with id not found
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No existing tokimon with id: " + id);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        // delete Tokimon by id
        tokimonMap.remove(id);
        saveToTokimonJson();

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @PostConstruct
    public void init() {
        System.out.println("POST CONSTRUCT CODE");

        // populate tokimonMap with existing tokimon in tokimon.json update counter for the tokimon id
        try {
            String jsonContents = Files.readString(tokimonJsonFile.toPath());
            List<Tokimon> tokimonList = gson.fromJson(jsonContents, getTokimonListType());
            if (tokimonList != null) {
                for (Tokimon t : tokimonList) {
                    tokimonMap.put(t.getId(), t);
                }
            }

            if (!tokimonMap.isEmpty()) {
                int maxId = Collections.max(tokimonMap.keySet());
                counter.set(maxId);
            }
        } catch (IOException e) {
            System.out.println("Error reading tokimon.json file.");
            e.printStackTrace();
        }
    }

    // Returns Type of List<Tokimon>:
    // Help from: https://stackoverflow.com/a/49043900/8930125
    private Type getTokimonListType() {
        return TypeToken.getParameterized(List.class, Tokimon.class).getType();
    }

    private void saveToTokimonJson() {
        try {
            writeToJson(tokimonJsonFile.getCanonicalPath(),
                    gson.toJson(new ArrayList<>(tokimonMap.values()))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Code from: https://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it-in-java
    private void writeToJson(String outputPath, String output) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath), StandardCharsets.UTF_8)) {
            writer.write(output);
        } catch (IOException e) {
            System.out.println("Error writing to tokimon.json file.");
            e.printStackTrace();
        }
    }
}