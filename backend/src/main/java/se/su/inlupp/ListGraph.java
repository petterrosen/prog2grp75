package se.su.inlupp;

//import java.util.Collection;
//import java.util.List;
//import java.util.Set;
import java.util.*;

public class ListGraph<T> implements Graph<T> {
  private final Map<T, Set<Edge<T>>> cities = new HashMap<>();


  @Override
  public void add(T node) {
    cities.putIfAbsent(node, new HashSet<>());
  }

  @Override
  public void connect(T node1, T node2, String name, int weight) {
    if (!cities.containsKey(node1) || !cities.containsKey(node2)) {
      throw new NoSuchElementException("One of the given cities does not exist");
    }
    if (weight < 0) {
      throw new IllegalArgumentException("Negative weight not allowed");
    }
    // Kasta om kant redan finns i någon riktning
    if (getEdgeBetween(node1, node2) != null) {
      throw new IllegalStateException("Edge already exists");
    }

    // Lägg till kanten i båda riktningarna
    cities.get(node1).add(new DirectionalEdge<>(node2, name, weight));
    cities.get(node2).add(new DirectionalEdge<>(node1, name, weight));
  }

  @Override
  public void setConnectionWeight(T node1, T node2, int weight) {
    if (!cities.containsKey(node1) || !cities.containsKey(node2)) {
      throw new NoSuchElementException("One of the given cities does not exist");
    }
    if (weight < 0) {
      throw new IllegalArgumentException("Weight cannot be less than zero!");
    }
    Edge<T> edge12 = getEdgeBetween(node1, node2);
    Edge<T> edge21 = getEdgeBetween(node2, node1);
    if (edge12 == null || edge21 == null) {
      throw new NoSuchElementException("There is no connection between these cities!");
    }
    edge12.setWeight(weight);
    edge21.setWeight(weight);
  }

  @Override
  public Set<T> getNodes() {
    return new HashSet<>(cities.keySet());
  }

  @Override
  public Collection<Edge<T>> getEdgesFrom(T node) {
    Set<Edge<T>> out = cities.get(node);
    if (out == null) throw new NoSuchElementException("The city doesn't exist!");
    return new HashSet<>(out);
  }

  @Override
  public Edge<T> getEdgeBetween(T node1, T node2) {
    Set<Edge<T>> out = cities.get(node1);
    if (out == null || !cities.containsKey(node2)) {
      throw new NoSuchElementException("One of the given cities does not exist");
    }
    return out.stream()
            .filter(e -> e.getDestination().equals(node2))
            .findFirst()
            .orElse(null);
  }

  @Override
  public void disconnect(T node1, T node2) {
    if (!cities.containsKey(node1) || !cities.containsKey(node2)) {
      throw new NoSuchElementException("One of the given cities does not exist");
    }
    Edge<T> e12 = getEdgeBetween(node1, node2);
    Edge<T> e21 = getEdgeBetween(node2, node1);
    if (e12 == null || e21 == null) {
      throw new IllegalStateException("Edge does not exist");
    }
    cities.get(node1).remove(e12);
    cities.get(node2).remove(e21);
  }

  @Override
  public void remove(T node) {
    if (!cities.containsKey(node)) {
      throw new NoSuchElementException("Node does not exist");
    }
    // Ta bort alla inkommande kanter först
    for (Edge<T> e : List.copyOf(cities.get(node))) {
      cities.get(e.getDestination()).removeIf(rev -> rev.getDestination().equals(node));
    }
    cities.remove(node);
  }

  @Override
  public boolean pathExists(T from, T to) {
    if (!cities.containsKey(from) || !cities.containsKey(to)) return false;
    var visited = new HashSet<T>();
    var stack = new ArrayDeque<T>();
    stack.push(from);
    while (!stack.isEmpty()) {
      T cur = stack.pop();
      if (cur.equals(to)) return true;
      if (visited.add(cur)) {
        for (Edge<T> e : cities.get(cur)) {
          if (!visited.contains(e.getDestination())) {
            stack.push(e.getDestination());
          }
        }
      }
    }
    return false;
  }

  @Override
  public List<Edge<T>> getPath(T from, T to) {
    if (!cities.containsKey(from) || !cities.containsKey(to)) return null;
    Queue<T> queue = new LinkedList<>();
    Map<T, T> pred = new HashMap<>();
    queue.add(from);
    pred.put(from, null);
    while (!queue.isEmpty()) {
      T cur = queue.poll();
      if (cur.equals(to)) break;
      for (Edge<T> e : cities.get(cur)) {
        T dst = e.getDestination();
        if (!pred.containsKey(dst)) {
          pred.put(dst, cur);
          queue.add(dst);
        }
      }
    }
    if (!pred.containsKey(to)) return null;
    // Bygg vägen baklänges
    List<Edge<T>> path = new LinkedList<>();
    for (T step = to; !step.equals(from); step = pred.get(step)) {
      path.add(0, getEdgeBetween(pred.get(step), step));
    }
    return path;
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    for (T node : cities.keySet()) {
      sb.append(node).append(":\n");
      for (Edge<T> e : cities.get(node)) {
        sb.append("  ").append(e).append("\n");
      }
    }
    return sb.toString();
  }
}