// PROG2 VT2025, inlämningsuppgift
// grupp 75
// Sama Matloub
// Yasin Akdeve
// Petter Rosén pero0033
package se.su.inlupp;

public interface Edge<T> {

  int getWeight();

  void setWeight(int weight);

  T getDestination();

  String getName();
}
