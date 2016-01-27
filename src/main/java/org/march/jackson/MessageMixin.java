package org.march.jackson;

import org.march.sync.endpoint.SynchronizationBucket;
import org.march.sync.endpoint.UpdateBucket;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@type")  
@JsonSubTypes({      
	@Type(value = SynchronizationBucket.class, name = "Update"),
    @Type(value = UpdateBucket.class, name = "Synchronize")})  
public abstract class MessageMixin {

}
