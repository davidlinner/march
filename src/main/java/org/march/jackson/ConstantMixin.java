package org.march.jackson;

import org.march.data.model.BooleanConstant;
import org.march.data.model.NumberConstant;
import org.march.data.model.StringConstant;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(using = ConstantDeserializer.class)
@JsonSerialize(using=ConstantSerializer.class)
@JsonSubTypes({      
	@Type(value = NumberConstant.class, name = "Constant"),
    @Type(value = BooleanConstant.class, name = "Constant"),
	@Type(value = StringConstant.class, name = "Constant")})
abstract public class ConstantMixin {

}
