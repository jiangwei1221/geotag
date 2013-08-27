package controllers;

import net.jiangwei.research.geo.tagging.services.GeoTaggingService;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class GeoTagging extends Controller {

	public static Result geotag(String url, String name) {
		GeoTaggingService gts = new GeoTaggingService();
		gts.process(url, name);
		if (gts.response() != null) {
			return ok(gts.response());
		} else {
			ObjectNode result = Json.newObject();

			ArrayNode contentArray = new ArrayNode(JsonNodeFactory.instance);
			for (String s : gts.contents) {
				contentArray.add(s);
			}
			result.put("content", contentArray);
			result.put("tags", Json.toJson(gts.geotags));

			return ok(result);
		}
	}
}
