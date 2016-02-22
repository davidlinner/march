package org.march.jackson;

import org.march.data.model.Constant;
import org.march.data.model.Pointer;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@type")  
@JsonSubTypes({      
	@Type(value = Constant.class, name = "Constant"),
    @Type(value = Pointer.class, name = "Pointer")})  
public abstract class DataMixin {

}
