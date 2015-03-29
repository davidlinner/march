package org.march.data;

public class Constant implements Data {
    
    private static final long serialVersionUID = -1767761499801107158L;
    
    private String value;
    
    private Primitive type;

    public Constant(String value, Primitive type) {
        super();
        this.value = value;
        this.type = type;
    }
    
    public Constant(String value) {
        super();
        this.value = value;
        this.type = Primitive.STRING;
    }
    
    public Constant(Double value) {
        super();
        this.value = Double.toString(value);
        this.type = Primitive.NUMBER;
    }

    public Constant(Integer value) {
        super();
        this.value = Integer.toString(value);
        this.type = Primitive.NUMBER;
    }

    public Constant(Boolean value) {
        super();
        this.value = Boolean.toString(value);
        this.type = Primitive.BOOLEAN;
    }


	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Primitive getType() {
		return type;
	}

	public void setType(Primitive type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Constant [value=" + value + ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Constant other = (Constant) obj;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

  
    
}
