package controllers;

import java.util.ArrayList;
import java.util.List;

import net.jiangwei.research.geo.tagging.services.GeoTaggingService;
import net.jiangwei.research.geo.tagging.services.RenderrableGeoTag;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class GeoTagging extends Controller {
	
	public static Result geotag(String url) {
		GeoTaggingService gts = new GeoTaggingService();
		gts.process(url);
		if (gts.response() != null) {
			return ok(gts.response());
		} else {
			ObjectNode result = Json.newObject();

			ArrayNode contentArray = new ArrayNode(JsonNodeFactory.instance);
			for (String s : gts.contents) {
				contentArray.add(s);
			}
			result.put("content", contentArray);
			result.put("tags", Json.toJson(select(gts.geotags, 0.5)));

			return ok(result);
		}
	}

	private static List<RenderrableGeoTag> select(
			List<RenderrableGeoTag> geotags, double threshold) {
		List<RenderrableGeoTag> result = new ArrayList<RenderrableGeoTag>();
		for (int i = 0; i < geotags.size(); i++) {
			if (geotags.get(i).getScore() >= threshold) {
				result.add(geotags.get(i));
			}
		}
		return result;
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result judge() {
		JsonNode json = request().body().asJson();
		String url = json.findPath("url").getTextValue();
		String name = json.findPath("name").getTextValue();
		String geotag = json.findPath("geotag").getTextValue();
		String correct = json.findPath("correct").getTextValue();
		String username = json.findPath("username").getTextValue();
		System.out.println("judge posted: " + url + "\n" + name + ", " + geotag
				+ ", " + correct + ", " + username);
		// asynchronous store
		JudgeStorer.getInstance().addTask(url, name, geotag, correct, username);
		return ok();
	}

}
