/*
 * Copyright (c) 2013 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.submission.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;

import org.icgc.dcc.submission.dictionary.model.Dictionary;
import org.icgc.dcc.submission.dictionary.model.Field;
import org.icgc.dcc.submission.dictionary.model.FileSchema;
import org.icgc.dcc.submission.fs.DccFileSystem;
import org.icgc.dcc.submission.fs.SubmissionDirectory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class RowColumnCheckerTest {

  @Mock
  private SubmissionDirectory submissionDir;
  @Mock
  private Dictionary dict;
  @Mock
  private DccFileSystem fs;

  @Before
  public void setup() {

    FileSchema testSchema = mock(FileSchema.class);
    String paramString = "testfile1";
    when(testSchema.getPattern()).thenReturn(paramString);
    when(dict.fileSchema(anyString())).thenReturn(Optional.of(testSchema));
    when(submissionDir.listFile()).thenReturn(ImmutableList.of("testfile1", "testfile2"));

    FileSchema fileSchema = mock(FileSchema.class);
    Optional<FileSchema> option = Optional.of(fileSchema);
    Field f1 = new Field();
    f1.setName("a");
    Field f2 = new Field();
    f2.setName("b");
    when(fileSchema.getFields()).thenReturn(ImmutableList.of(f1, f2));
    when(dict.fileSchema(anyString())).thenReturn(option);
  }

  @Test
  public void validColumns() throws Exception {
    DataInputStream fis = new DataInputStream(new ByteArrayInputStream("a\tb\rf1\tf2\r".getBytes()));
    PowerMockito.mockStatic(Util.class);
    when(Util.createInputStream(any(DccFileSystem.class), anyString())).thenReturn(fis);
    // when(submissionDir.listFile(any(Pattern.class))).thenReturn(ImmutableList.<String> of());

    RowColumnChecker checker = new RowColumnChecker(new BaseRowChecker(fs, dict, submissionDir));
    List<FirstPassValidationError> errors = checker.check(anyString());
    assertTrue(errors.isEmpty());
    assertTrue(checker.isValid());
  }

  @Test
  public void invalidColumnsHeader() throws Exception {
    DataInputStream fis = new DataInputStream(new ByteArrayInputStream("a\rf1\t\f2\r".getBytes()));
    PowerMockito.mockStatic(Util.class);
    when(Util.createInputStream(any(DccFileSystem.class), anyString())).thenReturn(fis);

    RowColumnChecker checker = new RowColumnChecker(new BaseRowChecker(fs, dict, submissionDir));
    List<FirstPassValidationError> errors = checker.check(anyString());
    assertFalse(errors.isEmpty());
    assertEquals(1, errors.size());
    assertFalse(checker.isValid());
  }

  @Test
  public void invalidColumnsContent() throws Exception {
    DataInputStream fis = new DataInputStream(new ByteArrayInputStream("a\tb\rf2\r".getBytes()));
    PowerMockito.mockStatic(Util.class);
    when(Util.createInputStream(any(DccFileSystem.class), anyString())).thenReturn(fis);

    RowColumnChecker checker = new RowColumnChecker(new BaseRowChecker(fs, dict, submissionDir));
    List<FirstPassValidationError> errors = checker.check(anyString());
    assertFalse(errors.isEmpty());
    assertEquals(1, errors.size());
    assertFalse(checker.isValid());
  }

  @Test
  public void invalidColumnsHeaderAndContent() throws Exception {
    DataInputStream fis = new DataInputStream(new ByteArrayInputStream("a\rf2\r".getBytes()));
    PowerMockito.mockStatic(Util.class);
    when(Util.createInputStream(any(DccFileSystem.class), anyString())).thenReturn(fis);

    RowColumnChecker checker = new RowColumnChecker(new BaseRowChecker(fs, dict, submissionDir));
    List<FirstPassValidationError> errors = checker.check(anyString());
    assertFalse(errors.isEmpty());
    assertEquals(2, errors.size());
    assertFalse(checker.isValid());
  }

  @Test
  public void invalidIrregularColumns() throws Exception {
    DataInputStream fis =
        new DataInputStream(new ByteArrayInputStream("a\tb\tc\rf1\tf2\tf3\tf3\tf4\r\f1\r".getBytes()));
    PowerMockito.mockStatic(Util.class);
    when(Util.createInputStream(any(DccFileSystem.class), anyString())).thenReturn(fis);

    RowColumnChecker checker = new RowColumnChecker(new BaseRowChecker(fs, dict, submissionDir));
    List<FirstPassValidationError> errors = checker.check(anyString());
    assertFalse(errors.isEmpty());
    assertEquals(3, errors.size());
    assertFalse(checker.isValid());
  }

  @Test
  public void validEmptyColumns() throws Exception {
    DataInputStream fis = new DataInputStream(new ByteArrayInputStream("\t\r\t\r".getBytes()));
    PowerMockito.mockStatic(Util.class);
    when(Util.createInputStream(any(DccFileSystem.class), anyString())).thenReturn(fis);

    RowColumnChecker checker = new RowColumnChecker(new BaseRowChecker(fs, dict, submissionDir));
    List<FirstPassValidationError> errors = checker.check(anyString());
    assertTrue(errors.isEmpty());
    assertTrue(checker.isValid());
  }
}
