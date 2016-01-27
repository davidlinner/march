package org.march.jackson;

import org.march.data.command.Construct;
import org.march.data.command.Delete;
import org.march.data.command.Destruct;
import org.march.data.command.Insert;
import org.march.data.command.Nil;
import org.march.data.command.Set;
import org.march.data.command.Unset;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@type")  
@JsonSubTypes({  
    @Type(value = Delete.class, name = "Delete"),  
    @Type(value = Insert.class, name = "Insert"),  
	@Type(value = Set.class, name = "Set"),
    @Type(value = Unset.class, name = "Unset"),  
    @Type(value = Nil.class, name = "Nil"),  
    @Type(value = Construct.class, name = "Nil"),
    @Type(value = Destruct.class, name = "Nil") })  
public abstract class CommandMixin {

}
