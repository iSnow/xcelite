module xcelite {
    requires guava;
    requires jsr305;
    requires lombok;
    requires poi;
    requires poi.ooxml;
    requires reflections;
    requires org.apache.commons.collections4;

    exports com.ebay.xcelite.annotations;
    exports com.ebay.xcelite.converters;
    exports com.ebay.xcelite.exceptions;
    exports com.ebay.xcelite.options;
    exports com.ebay.xcelite.policies;
    exports com.ebay.xcelite.reader;
    exports com.ebay.xcelite.sheet;
    exports com.ebay.xcelite.utils.diff;
    exports com.ebay.xcelite.writer;
}