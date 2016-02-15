package org.march.jackson;

import java.io.IOException;

import org.march.data.model.BooleanConstant;
import org.march.data.model.Constant;
import org.march.data.model.NumberConstant;
import org.march.data.model.StringConstant;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ConstantDeserializer extends JsonDeserializer<Constant<?>> {

	@Override
	public Constant<?> deserialize(JsonParser parser, DeserializationContext context) throws JsonProcessingException, IOException{
		JsonNode 	node 	= parser.getCodec().readTree(parser),
					value 	= node.get("value");
		
		if(value.isBoolean()){
			return new BooleanConstant(value.asBoolean());
		} else if (value.isTextual()){
			return new StringConstant(value.asText());
		} else if (value.isIntegralNumber()) {
			return new NumberConstant(value.asInt());
		} else if (value.isFloatingPointNumber()) {
			return new NumberConstant(value.asDouble());
		} else throw new JsonParseException("Constant type unknown.", parser.getCurrentLocation());			
	}

}
