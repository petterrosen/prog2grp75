// PROG2 VT2025, inlämningsuppgift del 1
// grupp 75
// Sama Matloub sama3201
// Yasin Akdeve yakk1087
// Petter Rosén pero0033

package se.su.inlupp;

public interface Edge<T> {

  int getWeight();

  void setWeight(int weight);

  T getDestination();

  String getName();
}
