/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.utils;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;

public class ContentFiles {

  private ContentFiles() {
    throw new UnsupportedOperationException();
  }

  public static boolean isDeleteFile(ContentFile<?> contentFile) {
    return !isDataFile(contentFile);
  }

  public static boolean isDataFile(ContentFile<?> contentFile) {
    return contentFile.content() == FileContent.DATA;
  }

  public static DataFile asDataFile(ContentFile<?> contentFile) {
    Preconditions.checkArgument(isDataFile(contentFile), "Not a data file");
    return (DataFile) contentFile;
  }

  public static DeleteFile asDeleteFile(ContentFile<?> contentFile) {
    Preconditions.checkArgument(isDeleteFile(contentFile), "Not a delete file");
    return (DeleteFile) contentFile;
  }
}
