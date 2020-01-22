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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

@AutoValue
abstract class Binding {
  abstract String getRole();
  abstract ImmutableList<String> getMembers();
  @Nullable abstract Condition getCondition();
  public abstract Builder toBuilder();

  static Builder newBuilder() {
    return new AutoValue_Binding.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setRole(String role);
    abstract Builder setMembers(List<String> members);
    abstract Builder setCondition(Condition condition);

    abstract String getRole();
    abstract ImmutableList<String> getMembers();
    abstract Condition getCondition();

    public abstract ImmutableList.Builder<String> membersBuilder();
    //public abstract Condition.Builder conditionBuilder();

    public Builder addMembers(String... members){
      for (String member : members) {
        membersBuilder().add(member);
      }
      return this;
    }

    public Builder removeMembers(String... members) {
      setMembers(ImmutableList.copyOf(Collections2.filter(getMembers(),
              Predicates.not(Predicates.in(Arrays.asList(members))))));
      return this;
    }

    abstract Binding build();
  }
}