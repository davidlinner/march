package org.march.jackson;

import java.io.IOException;

import org.march.data.BooleanConstant;
import org.march.data.Constant;
import org.march.data.NumberConstant;
import org.march.data.StringConstant;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

public class ConstantSerializer extends JsonSerializer<Constant<?>> {

	
	@Override
	public void serializeWithType(Constant<?> constant, JsonGenerator generator, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
		typeSer.writeTypePrefixForObject(constant, generator);
		serialize(constant, generator, provider);
		typeSer.writeTypeSuffixForObject(constant, generator);
	}

	@Override
	public void serialize(Constant<?> constant, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
				 
		 if(constant instanceof NumberConstant) {	
			 Number number = ((NumberConstant)constant).getValue();
			 if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte){
				 generator.writeNumberField("value", number.longValue());
			 } else {
				 generator.writeNumberField("value", number.doubleValue());
			 }			 
		 } else if (constant instanceof BooleanConstant)		  
			 generator.writeBooleanField("value", ((BooleanConstant)constant).getValue()); 
		 else if (constant instanceof StringConstant)
			 generator.writeStringField("value", ((StringConstant)constant).getValue());
		 
		 
	}

}
