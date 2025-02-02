/**
 * rscplus
 *
 * <p>This file is part of rscplus.
 *
 * <p>rscplus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscplus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscplus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/RSCPlus/rscplus>
 */
package Client.Extensions;

import Client.ServerExtensions;
import Client.Util;
import Client.World;

/**
 * Defines a distinct "World Type", as it relates to a known {@link
 * ServerExtensions.WorldSubscription}.
 *
 * <p>{@link World} data obtained from a subscription API may optionally contain a {@link
 * World#WORLD_ID}, used to label distinct worlds from a particular server host. For example, an
 * operator may choose to offer a set of multiple interconnected worlds, allowing players to
 * pick-and-choose one to connect to. For this example, let's imagine these IDs to be "members1",
 * "members2", and "members3". The identifier for the currently-selected world within RSCPlus (e.g.
 * "members2") may then be used to query a population API, such that the current count may be
 * displayed to the user on the login screen.
 *
 * <p>A {@link WorldType} would be used as a means of identifying a collection of world IDs
 * belonging to a distinct group. The worlds described above would then belong to a {@link
 * WorldType} constructed from the String value {@code Members}. To expand on the previous example,
 * let's imagine that the same server host also offers a set of veteran worlds, restricted to
 * players until they reach a certain playtime or level-based milestone. For this example, these
 * worlds would be "veteran1" and "veteran2", identified by a {@link WorldType} constructed from the
 * value {@code Veteran}.
 *
 * <p>To illustrate further, the server host may want the client to conditionally display a special
 * badge on the login screen, denoting that the currently-selected world is a {@code Veteran} type.
 * They could then invoke the {@link #matchesWorldId(String)} to quickly verify that the selected
 * world is indeed the correct type.
 *
 * <p>For logical consistency, {@link WorldType} identifier values <b>MUST</b> only be alpha values,
 * and world IDs <b>MUST</b> be equal to or begin with the same value, and <b>MUST NOT</b> end in
 * anything other than a numerical value, i.e. matching the following regex: {@code [a-zA-Z]*\d?}.
 */
public final class WorldType {
  private final String name;
  private final int hash;

  /**
   * Internal constructor
   *
   * @param name {@link String} value identifying the {@link WorldType}
   */
  private WorldType(String name) {
    if (Util.isBlank(name)) {
      throw new RuntimeException("Type name cannot be null or empty");
    }

    this.name = name;
    this.hash = name.toLowerCase().hashCode();
  }

  /**
   * External-facing constructor
   *
   * @param name {@link String} value identifying the {@link WorldType}
   * @return Constructed {@link WorldType} instance
   */
  public static WorldType from(String name) {
    return new WorldType(name);
  }

  /** @return the {@link WorldType} named identifier */
  public String getName() {
    return name;
  }

  /**
   * Checks for a match between a provided {@link World#WORLD_ID} value and this instance of {@link
   * WorldType}, following the rules prescribed in the class-level documentation for {@link
   * WorldType}.
   *
   * @param worldId {@code String} value to check against
   * @return {@code boolean} value indicating type equivalence
   */
  public boolean matchesWorldId(String worldId) {
    if (Util.isBlank(worldId)) {
      return false;
    }

    final String normalizedWorldId = worldId.toLowerCase();

    // Must start with the worldType
    if (!normalizedWorldId.startsWith(name.toLowerCase())) {
      return false;
    }

    // Any remaining characters must be empty or resolve to an integer value
    String worldNum = normalizedWorldId.replace(name.toLowerCase(), "");

    if (!worldNum.isEmpty()) {
      try {
        Integer.parseInt(worldNum);
      } catch (NumberFormatException nfe) {
        return false;
      }
    }

    return true;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return name.equalsIgnoreCase(((WorldType) o).getName());
  }

  @Override
  public int hashCode() {
    return hash;
  }
}
