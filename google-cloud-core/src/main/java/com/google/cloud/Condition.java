/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud;

import com.google.api.core.BetaApi;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@BetaApi
@AutoValue
public abstract class Condition {
  @Nullable
  public abstract String getTitle();

  @Nullable
  public abstract String getDescription();

  @Nullable
  public abstract String getExpression();

  public abstract Builder toBuilder();

  public static Builder newBuilder() {
    return new AutoValue_Condition.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setTitle(String title);

    public abstract Builder setDescription(String description);

    public abstract Builder setExpression(String expression);

    public abstract Condition build();
  }
}
