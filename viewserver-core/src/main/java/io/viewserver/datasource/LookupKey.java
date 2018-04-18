package io.viewserver.datasource;

public class LookupKey {
    private String dimensionNamespace;
    private String dimensionname;

    public LookupKey(String dimensionNamespace, String  dimensionname) {
        this.dimensionNamespace = dimensionNamespace;
        this.dimensionname = dimensionname;
    }

    @Override
    public String toString() {
        return String.format("%s/%s", dimensionNamespace, dimensionname);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LookupKey lookupKey = (LookupKey) o;

        if (!dimensionNamespace.equals(lookupKey.dimensionNamespace))
            return false;
        return dimensionname.equals(lookupKey.dimensionname);
    }

    @Override
    public int hashCode() {
        int result = dimensionNamespace != null ? dimensionNamespace.hashCode() : 0;
        result = 31 * result + dimensionname.hashCode();
        return result;
    }
}
