package us.poliscore.legiscan;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.SneakyThrows;
import us.poliscore.legiscan.service.CachedLegiscanService;
import us.poliscore.legiscan.service.LegiscanService;

public class LegiscanClient {
	@SneakyThrows
    public static void main(String[] args) {
        Options options = new Options();

        options.addRequiredOption("k", "key", true, "LegiScan API key");
        options.addRequiredOption("op", "operation", true, "Operation to perform. Valid values: cacheDataset, getBill, getBillText, getAmendment,\n" +
        	    "getSupplement, getRollCall, getPerson, getSessionList, getMasterList,\n" +
        	    "getMasterListRaw, getSearch, getSearchRaw, getDatasetList, getDataset,\n" +
        	    "getDatasetRaw, getSessionPeople, getSponsoredList, getMonitorList, getMonitorListRaw, setMonitor");

        options.addOption("i", "id", true, "ID for operations requiring a bill/session/person ID");
        options.addOption("s", "state", true, "State abbreviation (e.g., CA, TX)");
        options.addOption("y", "year", true, "Year filter (e.g., 2024)");
        options.addOption("sp", "special", false, "Special. Used for cacheDataset. (default: false)");
        options.addOption("q", "query", true, "Query string for search");
        options.addOption("a", "access_key", true, "Access key for dataset retrieval");
        options.addOption("f", "format", true, "Format for dataset (json, csv)");
        options.addOption("p", "page", true, "Page number for paginated search");
        options.addOption("m", "monitor_ids", true, "Comma-separated list of bill IDs to monitor (required for setMonitor)");
        options.addOption("ac", "action", true, "Action to take for setMonitor: monitor, remove, or set");
        options.addOption("st", "stance", true, "Stance to apply (optional, defaults to 'watch')");
        options.addOption("r", "record", true, "Record filter for monitor list (current, archived, year)");

        options.addOption("c", "no_cache", false, "Disable caching (enabled by default)");
        options.addOption("cd", "cache_dir", true, "Directory to use for cached data. (default: <user.home>/appdata/poliscore/legiscan)");
        options.addOption("ct", "cache_ttl", true, "Time to live for cached items in seconds (default: 14400)");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("ERROR: " + e.getMessage());
            formatter.printHelp("LegiscanClient", options);
            System.exit(1);
            return;
        }

        String apiKey = cmd.getOptionValue("key");
        String op = cmd.getOptionValue("operation");
        
        validateRequiredArgs(cmd, op);

        LegiscanService service;
        if (cmd.hasOption("no_cache") && !op.equals("cacheDataset")) {
            service = new LegiscanService(apiKey);
        } else {
            CachedLegiscanService.Builder builder = CachedLegiscanService.builder(apiKey);

            if (cmd.hasOption("cache_dir")) {
                builder.withCacheDirectory(new File(cmd.getOptionValue("cache_dir")));
            }

            if (cmd.hasOption("cache_ttl")) {
                builder.withCacheTttl(Integer.parseInt(cmd.getOptionValue("cache_ttl")));
            }

            service = builder.build();
        }

        ObjectMapper outputMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

        switch (op) {
        	case "cacheDataset" -> {
        		var cacheService = (CachedLegiscanService)service;
        		var cached = cacheService.cacheDataset(cmd.getOptionValue("state"), Integer.parseInt(cmd.getOptionValue("year")), cmd.hasOption("special"));
        		System.out.println("Successfully loaded [" + cached.getDataset().getSessionName() + "] into cache [" + cacheService.getCache().toString() + "]. Dataset contains " + cached.getPeople().size() + " people, " + cached.getBills().size()+ " bills, and " + cached.getVotes().size()+ " votes.");
        	}
            case "getBill" -> System.out.println(outputMapper.writeValueAsString(service.getBill(Integer.parseInt(cmd.getOptionValue("id")))));
            case "getBillText" -> System.out.println(outputMapper.writeValueAsString(service.getBillText(Integer.parseInt(cmd.getOptionValue("id")))));
            case "getAmendment" -> System.out.println(outputMapper.writeValueAsString(service.getAmendment(Integer.parseInt(cmd.getOptionValue("id")))));
            case "getSupplement" -> System.out.println(outputMapper.writeValueAsString(service.getSupplement(Integer.parseInt(cmd.getOptionValue("id")))));
            case "getRollCall" -> System.out.println(outputMapper.writeValueAsString(service.getRollCall(Integer.parseInt(cmd.getOptionValue("id")))));
            case "getPerson" -> System.out.println(outputMapper.writeValueAsString(service.getPerson(Integer.parseInt(cmd.getOptionValue("id")))));
            case "getSessionList" -> System.out.println(outputMapper.writeValueAsString(service.getSessionList(cmd.getOptionValue("state"))));
            case "getMasterList" -> {
                if (cmd.hasOption("id")) System.out.println(outputMapper.writeValueAsString(service.getMasterList(Integer.parseInt(cmd.getOptionValue("id")))));
                else if (cmd.hasOption("state")) System.out.println(outputMapper.writeValueAsString(service.getMasterList(cmd.getOptionValue("state"))));
                else throw new IllegalArgumentException("getMasterList requires --id or --state");
            }
            case "getMasterListRaw" -> {
                if (cmd.hasOption("id")) System.out.println(outputMapper.writeValueAsString(service.getMasterListRaw(Integer.parseInt(cmd.getOptionValue("id")))));
                else if (cmd.hasOption("state")) System.out.println(outputMapper.writeValueAsString(service.getMasterListRaw(cmd.getOptionValue("state"))));
                else throw new IllegalArgumentException("getMasterListRaw requires --id or --state");
            }
            case "getSearch" -> {
                String query = cmd.getOptionValue("query");
                int page = Integer.parseInt(cmd.getOptionValue("page", "1"));
                if (cmd.hasOption("id"))
                    System.out.println(outputMapper.writeValueAsString(service.getSearch(Integer.parseInt(cmd.getOptionValue("id")), query, page)));
                else
                    System.out.println(outputMapper.writeValueAsString(service.getSearch(cmd.getOptionValue("state"), query,
                            Integer.parseInt(cmd.getOptionValue("year", "2")), page)));
            }
            case "getSearchRaw" -> {
                String query = cmd.getOptionValue("query");
                int page = Integer.parseInt(cmd.getOptionValue("page", "1"));
                if (cmd.hasOption("id"))
                    System.out.println(outputMapper.writeValueAsString(service.getSearchRaw(Integer.parseInt(cmd.getOptionValue("id")), query, page)));
                else
                    System.out.println(outputMapper.writeValueAsString(service.getSearchRaw(cmd.getOptionValue("state"), query,
                            Integer.parseInt(cmd.getOptionValue("year", "2")),
                            cmd.hasOption("id") ? Integer.parseInt(cmd.getOptionValue("id")) : null,
                            page)));
            }
            case "getDatasetList" -> System.out.println(outputMapper.writeValueAsString(service.getDatasetList(cmd.getOptionValue("state"),
                    cmd.hasOption("year") ? Integer.parseInt(cmd.getOptionValue("year")) : null)));
            case "getDataset" -> System.out.println(outputMapper.writeValueAsString(service.getDataset(
                    Integer.parseInt(cmd.getOptionValue("id")),
                    cmd.getOptionValue("access_key"),
                    cmd.getOptionValue("format", "json")
            )));
            case "getDatasetRaw" -> System.out.write(service.getDatasetRaw(
                    Integer.parseInt(cmd.getOptionValue("id")),
                    cmd.getOptionValue("access_key"),
                    cmd.getOptionValue("format", "json")
            ));
            case "getSessionPeople" -> System.out.println(outputMapper.writeValueAsString(service.getSessionPeople(Integer.parseInt(cmd.getOptionValue("id")))));
            case "getSponsoredList" -> System.out.println(outputMapper.writeValueAsString(service.getSponsoredList(Integer.parseInt(cmd.getOptionValue("id")))));
            case "getMonitorList" -> {
                String record = cmd.getOptionValue("record", "current");
                System.out.println(outputMapper.writeValueAsString(service.getMonitorList(record)));
            }
            case "getMonitorListRaw" -> {
                String record = cmd.getOptionValue("record", "current");
                System.out.println(outputMapper.writeValueAsString(service.getMonitorListRaw(record)));
            }
            case "setMonitor" -> {
                require(cmd, "monitor_ids");
                require(cmd, "action");
                List<Integer> ids = List.of(cmd.getOptionValue("monitor_ids").split(","))
                                        .stream()
                                        .map(String::trim)
                                        .map(Integer::parseInt)
                                        .toList();
                String action = cmd.getOptionValue("action");
                String stance = cmd.getOptionValue("stance", "watch");
                System.out.println(outputMapper.writeValueAsString(service.setMonitor(ids, action, stance)));
            }

            default -> throw new IllegalArgumentException("Unknown operation: " + op);
        }
    }
    
    private static void validateRequiredArgs(CommandLine cmd, String op) {
        switch (op) {
            case "cacheDataset" -> {
                require(cmd, "state");
                require(cmd, "year");
            }
            case "getBill", "getBillText", "getAmendment", "getSupplement", "getRollCall",
                 "getPerson", "getDataset", "getDatasetRaw", "getSessionPeople", "getSponsoredList" -> {
                require(cmd, "id");
            }
            case "getSessionList" -> {
                require(cmd, "state");
            }
            case "getMasterList", "getMasterListRaw" -> {
                if (!cmd.hasOption("id") && !cmd.hasOption("state")) {
                    throw new IllegalArgumentException(op + " requires either --id or --state");
                }
            }
            case "getSearch", "getSearchRaw" -> {
                require(cmd, "query");
                if (!cmd.hasOption("id") && !cmd.hasOption("state")) {
                    throw new IllegalArgumentException(op + " requires either --id or --state");
                }
            }
            case "getDatasetList" -> {
                require(cmd, "state");
            }
            case "getMonitorList", "getMonitorListRaw" -> {
                // Optional --record, no required args
            }
            case "setMonitor" -> {
                require(cmd, "monitor_ids");
                require(cmd, "action");
            }
            default -> throw new IllegalArgumentException("Unknown operation: " + op);
        }
    }

    private static void require(CommandLine cmd, String opt) {
        if (!cmd.hasOption(opt)) {
            throw new IllegalArgumentException("Missing required option: --" + opt);
        }
    }
}
