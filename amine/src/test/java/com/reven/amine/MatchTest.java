package com.reven.amine;

import org.junit.Test;

public class MatchTest {
    @Test
    public void match() throws Exception {
        assert "0551123".matches("0551123");
        assert !"0551124".matches("0551123");
        assert "0551123".matches("^0551.*");
        assert !"10551123".matches("^0551.*");
        assert "1230551123".matches(".*0551.*");
        assert !"12305151123".matches(".*0551.*");
        assert "1230551".matches(".*0551$");
        assert !"12305511".matches(".*0551$");
    }

    @Test
    public void test() throws Exception{
    }
}
