package org.opencypher.grammar;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.opencypher.grammar.CodePointSet.codePoints;
import static org.opencypher.grammar.CodePointSet.range;
import static org.opencypher.grammar.CodePointSet.single;
import static org.opencypher.grammar.CodePointSet.union;

@RunWith(Enclosed.class)
public class CodePointSetTest
{
    @RunWith(Parameterized.class)
    public static class Parser
    {
        private final ParsingTest test;

        public Parser( ParsingTest test )
        {
            this.test = test;
        }

        @Test
        public void parse() throws Exception
        {
            test.parsing();
        }

        @Parameterized.Parameters(name = "{0}")
        public static List<Object[]> tests()
        {
            return Arrays.asList(
                    parses( "[a]", single( 'a' ) ),
                    parses( "[a-z]", range( 'a', 'z' ) ),
                    parses( "[a-z0-9]", range( 'a', 'z' ), range( '0', '9' ) ),
                    parses( "[aoueiy]", codePoints( 'a', 'o', 'u', 'e', 'i', 'y' ) ),
                    parses( "[\\a]", single( 0x07 ) ),
                    parses( "[\\b]", single( '\b' ) ),
                    parses( "[\\e]", single( 0x1B ) ),
                    parses( "[\\f]", single( '\f' ) ),
                    parses( "[\\n]", single( '\n' ) ),
                    parses( "[\\r]", single( '\r' ) ),
                    parses( "[\\t]", single( '\t' ) ),
                    parses( "[\\v]", single( 0x0B ) ),
                    fails( "[a-]", containsString( "cannot end in '-' or '\\'" ) ),
                    fails( "[a--b]", containsString( "'-' may not follow '-'" ) ),
                    fails( "[-z]", containsString( "'-' must be preceded by single char" ) ),
                    fails( "[\\]", containsString( "cannot end in '-' or '\\'" ) ),
                    fails( "[\\x]", containsString( "Invalid escape character" ) ),
                    fails( "a-z", containsString( "must be enclosed in '[...]" ) ),
                    fails( "[a-z", containsString( "must be enclosed in '[...]" ) ),
                    fails( "a-z]", containsString( "must be enclosed in '[...]" ) )
            );
        }
    }

    static abstract class ParsingTest
    {
        final String input;

        ParsingTest( String input )
        {
            this.input = input;
        }

        abstract void parsing();

        @Override
        public String toString()
        {
            switch ( getClass().getEnclosingMethod().getName() )
            {
            case "parses":
                return "should parse \"" + input + '"';
            case "fails":
                return "should fail to parse \"" + input + '"';
            default:
                return '"' + input + '"';
            }
        }
    }

    private static Object[] parses( String input, CodePointSet... expected )
    {
        return new Object[]{new ParsingTest( input )
        {
            @Override
            void parsing()
            {
                assertParses( input, expected );
            }
        }};
    }

    private static Object[] fails( String input, Matcher<String> message )
    {
        return new Object[]{new ParsingTest( input )
        {
            @Override
            void parsing()
            {
                assertParsingFails( input, message );
            }
        }};
    }

    static void assertParses( String input, CodePointSet... expected )
    {
        assertEquals( union( expected ), CodePointSet.parse( input ) );
    }

    static void assertParsingFails( String input, Matcher<String> message )
    {
        try
        {
            CodePointSet.parse( input );
        }
        catch ( IllegalArgumentException e )
        {
            assertThat( e.getMessage(), message );
        }
    }
}
