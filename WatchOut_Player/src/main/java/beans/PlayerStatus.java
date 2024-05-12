/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;


public enum PlayerStatus 
{
    Active,
    Seeker, //assigned only to an elected seeker
    Moving, //for acquired H.B. permission
    Safe,
    Tagged
}
