package org.apache.lucene.analysis.position;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

public class PositionFilterTest extends TestCase {

  public class TestTokenStream extends TokenStream {

    protected int index = 0;
    protected String[] testToken;
    protected TermAttribute termAtt;

    public TestTokenStream(String[] testToken) {
      super();
      this.testToken = testToken;
      termAtt = (TermAttribute) addAttribute(TermAttribute.class);
    }

    public final boolean incrementToken() throws IOException {
      if (index < testToken.length) {
        termAtt.setTermBuffer(testToken[index++]);
        return true;
      } else {
        return false;
      }
    }
    public void reset() {
      index = 0;
    }
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(PositionFilterTest.class);
  }
  public static final String[] TEST_TOKEN = new String[]{
    "please",
    "divide",
    "this",
    "sentence",
    "into",
    "shingles",
  };
  public static final int[] TEST_TOKEN_POSITION_INCREMENTS = new int[]{
    1, 0, 0, 0, 0, 0
  };
  public static final int[] TEST_TOKEN_NON_ZERO_POSITION_INCREMENTS = new int[]{
    1, 5, 5, 5, 5, 5
  };

  public static final String[] SIX_GRAM_NO_POSITIONS_TOKENS = new String[]{
    "please",
    "please divide",
    "please divide this",
    "please divide this sentence",
    "please divide this sentence into",
    "please divide this sentence into shingles",
    "divide",
    "divide this",
    "divide this sentence",
    "divide this sentence into",
    "divide this sentence into shingles",
    "this",
    "this sentence",
    "this sentence into",
    "this sentence into shingles",
    "sentence",
    "sentence into",
    "sentence into shingles",
    "into",
    "into shingles",
    "shingles",
  };
  public static final int[] SIX_GRAM_NO_POSITIONS_INCREMENTS = new int[]{
    1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
  };
  public static final String[] SIX_GRAM_NO_POSITIONS_TYPES = new String[]{
    "word", "shingle", "shingle", "shingle", "shingle", "shingle",
    "word", "shingle", "shingle", "shingle", "shingle",
    "word", "shingle", "shingle", "shingle",
    "word", "shingle", "shingle",
    "word", "shingle",
    "word"
  };

  public void testFilter() throws IOException {

    filterTest(new PositionFilter(new TestTokenStream(TEST_TOKEN)),
               TEST_TOKEN,
               TEST_TOKEN_POSITION_INCREMENTS);
  }

  public void testNonZeroPositionIncrement() throws IOException {
    
    filterTest(new PositionFilter(new TestTokenStream(TEST_TOKEN), 5),
               TEST_TOKEN,
               TEST_TOKEN_NON_ZERO_POSITION_INCREMENTS);
  }
  
  public void testReset() throws IOException {

    PositionFilter filter = new PositionFilter(new TestTokenStream(TEST_TOKEN));
    filterTest(filter, TEST_TOKEN, TEST_TOKEN_POSITION_INCREMENTS);
    filter.reset();
    // Make sure that the reset filter provides correct position increments
    filterTest(filter, TEST_TOKEN, TEST_TOKEN_POSITION_INCREMENTS);
  }
  
  /** Tests ShingleFilter up to six shingles against six terms.
   *  Tests PositionFilter setting all but the first positionIncrement to zero.
   * @throws java.io.IOException @see Token#next(Token)
   */
  public void test6GramFilterNoPositions() throws IOException {

    ShingleFilter filter = new ShingleFilter(new TestTokenStream(TEST_TOKEN), 6);
    filterTest(new PositionFilter(filter),
               SIX_GRAM_NO_POSITIONS_TOKENS,
               SIX_GRAM_NO_POSITIONS_INCREMENTS);
  }

  protected TokenStream filterTest(final TokenStream filter,
                                   final String[] tokensToCompare,
                                   final int[] positionIncrements)
      throws IOException {

    int i = 0;
    final Token reusableToken = new Token();

    for (Token nextToken = filter.next(reusableToken)
        ; i < tokensToCompare.length
        ; nextToken = filter.next(reusableToken)) {

      if (null != nextToken) {
        final String termText = nextToken.term();
        final String goldText = tokensToCompare[i];

        assertEquals("Wrong termText", goldText, termText);
        assertEquals("Wrong positionIncrement for token \"" + termText + "\"",
                     positionIncrements[i], nextToken.getPositionIncrement());
      }else{
        assertNull(tokensToCompare[i]);
      }
      i++;
    }
    return filter;
  }
}
