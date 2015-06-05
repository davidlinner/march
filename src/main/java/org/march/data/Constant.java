package org.march.data;

public abstract class Constant<T> implements Data {
    
    private static final long serialVersionUID = -1767761499801107158L;
    
    private T value;
    
    public Constant(T value /*, Primitive type*/) {
        super();
        this.value = value;
    }
   

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		Constant<?> other = (Constant<?>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Constant [value=" + value + "]";
	}  
    
}
