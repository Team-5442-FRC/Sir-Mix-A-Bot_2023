// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.sensors.CANCoder;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.Constants.SwerveConstants;

public class SwerveUnit extends SubsystemBase {

    /*----- Independent Variables -----*/
    boolean driveInverted = false;
    boolean steerInverted = false;

    double driveMotorSpeed, steerMotorSpeed;
    double rawAngle, desiredAngle, difference;

    double zeroOffset;



    /*----- Initialize Motors and Electrics -----*/
    TalonFX rotationMotor, driveMotor;
    CANCoder encoder;


  /** Creates a new SwerveUnit. */
  public SwerveUnit(TalonFX driveMotor, TalonFX rotationMotor, CANCoder encoder, boolean driveInverted, boolean steerInverted, double zeroOffset) {
    this.rotationMotor = rotationMotor;
    this.driveMotor = driveMotor;
    this.encoder = encoder;
    this.driveInverted = driveInverted;
    this.steerInverted = steerInverted;
    this.zeroOffset = zeroOffset;
  }

  public void move(double fv, double desiredAngle) { //fv is in meters/second, desiredAngle is in degrees

    if(desiredAngle < 0) {
      desiredAngle +=360;
    }

    difference = desiredAngle - getRawAngle();

    if(difference < 0) {
      difference = 0; 
    }
    if(difference > 90) {
      difference = 90;
    }

    this.driveMotorSpeed = fv * SwerveConstants.SpeedMultiplier * ((90 - Math.abs(difference))/90); //((60 * fv)/SwerveConstants.WheelCircumferenceM) //* SwerveConstants.SWERVE_GEAR_RATIO_DRIVE;

    if(this.driveInverted) {
      driveMotorSpeed *= -1;
    }
    this.steerMotorSpeed = steerSpeed(getRawAngle(), desiredAngle);

    this.desiredAngle = desiredAngle;
  }

  public double steerSpeed(double currentAngle, double desiredAngle) {
    //at 0 degrees from the angle we want he rotation speed to be 0
    // at 180 degrees we have a constant in constants for our maximum speed
    //these two points can make a linear graph

    double difference = desiredAngle - currentAngle;
    if(difference > 180) {
      difference -= 360;
    }
    if(difference < -180) {
      difference += 360;
    }

    this.difference = difference;
    if(Math.abs(difference) < 6) {
      return 0;
    }

    return SwerveConstants.AdditionalTurnSpeed * (difference/180) + (SwerveConstants.MinModuleTurnSpeed * (difference/Math.abs(difference)));

  }

  public double getRawAngle() {
    this.rawAngle = this.encoder.getAbsolutePosition() - this.zeroOffset;
    if(this.rawAngle < 0) {
      this.rawAngle += 360;
    }
    return this.rawAngle;
  }

  @Override
  public void periodic() {
  }

  public void updateMotorSpeeds() {
    this.driveMotor.set(ControlMode.PercentOutput, this.driveMotorSpeed);
    this.rotationMotor.set(ControlMode.PercentOutput, this.steerMotorSpeed);
  }
}
