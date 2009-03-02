package org.yaml.snakeyaml.resolver;

import junit.framework.TestCase;

public class RagelMachineTest extends TestCase {
    private RagelMachine machine = new RagelMachine();

    public void testScan() {
        assertNull(machine.scan("abc"));
    }

    public void testNullPointerException() {
        try {
            machine.scan(null);
            fail("null must not be accepted.");
        } catch (NullPointerException e) {
            assertEquals("Scalar must be provided", e.getMessage());
        }
    }

    public void testScanBoolean() {
        assertEquals("tag:yaml.org,2002:bool", machine.scan("true"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("True"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("TRUE"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("false"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("False"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("FALSE"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("on"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("ON"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("On"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("off"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("Off"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("OFF"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("on"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("ON"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("On"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("off"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("Off"));
        assertEquals("tag:yaml.org,2002:bool", machine.scan("OFF"));
    }

    public void testScanNull() {
        assertEquals("tag:yaml.org,2002:null", machine.scan("null"));
        assertEquals("tag:yaml.org,2002:null", machine.scan("Null"));
        assertEquals("tag:yaml.org,2002:null", machine.scan("NULL"));
        assertEquals("tag:yaml.org,2002:null", machine.scan("~"));
        assertEquals("tag:yaml.org,2002:null", machine.scan(" "));
    }

    public void testScanMerge() {
        assertEquals("tag:yaml.org,2002:merge", machine.scan("<<"));
    }

    public void testScanValue() {
        assertEquals("tag:yaml.org,2002:value", machine.scan("="));
    }

    public void testScanInt() {
        assertEquals("tag:yaml.org,2002:int", machine.scan("0"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("1"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("-0"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("-9"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("0b0011"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("0x12ef"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("0123"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("1_000"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("1_000_000"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("+0"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("+10"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("1__000"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("24:12:34"));
        assertEquals("tag:yaml.org,2002:int", machine.scan("240:12:34"));
    }

    public void testScanFloat() {
        assertEquals("tag:yaml.org,2002:float", machine.scan("1.0"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("-0.0"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("+2.2310"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("1.0e+12"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("1.345e-3"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("190:20:30.15"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("-.inf"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("+.INF"));
        assertEquals("tag:yaml.org,2002:float", machine.scan(".Inf"));
        assertEquals("tag:yaml.org,2002:float", machine.scan(".nan"));
        assertEquals("tag:yaml.org,2002:float", machine.scan(".NaN"));
        assertEquals("tag:yaml.org,2002:float", machine.scan(".NAN"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("1_000.5"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("1.023_456"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("-1_123.45"));
        assertEquals("tag:yaml.org,2002:float", machine.scan(".5"));
        assertEquals("tag:yaml.org,2002:float", machine.scan("1.E+1"));
    }
}
