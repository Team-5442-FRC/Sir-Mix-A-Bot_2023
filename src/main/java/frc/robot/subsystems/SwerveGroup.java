// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.sensors.CANCoder;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotContainer;
import frc.robot.Constants.SwerveConstants;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class SwerveGroup extends SubsystemBase {
  /*----- Initialize Swerve Modules -----*/
  static SwerveUnit FR;
  static SwerveUnit FL;
  static SwerveUnit BL;
  static SwerveUnit BR;

  static CANCoder cFR;
  static CANCoder cFL;
  static CANCoder cBL;
  static CANCoder cBR;
  AHRS navx;


  /** Creates a new SwerveGroup. */
  public SwerveGroup() {
    navx = new AHRS(SerialPort.Port.kMXP);
    //Grab everything where it was initialized in RobotContainer
    FR = RobotContainer.FR;
    FL = RobotContainer.FL;
    BL = RobotContainer.BL;
    BR = RobotContainer.BR;

    cFR = RobotContainer.cFR;
    cFL = RobotContainer.cFL;
    cBL = RobotContainer.cBL;
    cBR = RobotContainer.cBR;


  }

  public void Drive(double vx, double vy, double vr) { //This is being mean trying something else
    Translation2d m_frontRight = new Translation2d(SwerveConstants.TrackwidthM/2,-SwerveConstants.WheelbaseM/2); //Making 2D translations from the center of the robot to the swerve modules
    Translation2d m_frontLeft = new Translation2d(SwerveConstants.TrackwidthM/2,SwerveConstants.WheelbaseM/2);
    Translation2d m_backLeft = new Translation2d(-SwerveConstants.TrackwidthM/2,SwerveConstants.WheelbaseM/2);
    Translation2d m_backRight = new Translation2d(-SwerveConstants.TrackwidthM/2,-SwerveConstants.WheelbaseM/2);

    SwerveDriveKinematics m_Kinematics = new SwerveDriveKinematics(m_frontRight, m_frontLeft, m_backLeft, m_backRight);

    ChassisSpeeds speeds = ChassisSpeeds.fromFieldRelativeSpeeds(vy, vx, vr, Rotation2d.fromDegrees(-navx.getYaw()));

    SwerveModuleState[] moduleStates = m_Kinematics.toSwerveModuleStates(speeds);

    SwerveModuleState frontRight = moduleStates[0];
    SwerveModuleState frontRightOptimized = SwerveModuleState.optimize(frontRight, Rotation2d.fromDegrees(FR.getRawAngle()));
    SwerveModuleState frontLeft = moduleStates[1];
    SwerveModuleState frontLeftOptimized = SwerveModuleState.optimize(frontLeft, Rotation2d.fromDegrees(FL.getRawAngle()));
    SwerveModuleState backLeft = moduleStates[2];
    SwerveModuleState backLeftOptimized = SwerveModuleState.optimize(backLeft, Rotation2d.fromDegrees(BL.getRawAngle()));
    SwerveModuleState backRight = moduleStates[3];
    SwerveModuleState backRightOptimized = SwerveModuleState.optimize(backRight, Rotation2d.fromDegrees(BR.getRawAngle()));

    FL.move(frontLeftOptimized.speedMetersPerSecond, frontLeftOptimized.angle.getDegrees());
    FR.move(frontRightOptimized.speedMetersPerSecond, frontRightOptimized.angle.getDegrees());
    BL.move(backLeftOptimized.speedMetersPerSecond, backLeftOptimized.angle.getDegrees());
    BR.move(backRightOptimized.speedMetersPerSecond, backRightOptimized.angle.getDegrees());

    SmartDashboard.putNumber("NavX Angle", -navx.getYaw());

  }

  public AHRS getNavX() { //for commands 
    return this.navx;
  }


  public void CustomDrive(double x, double y, double r) {
    //Find angle of the two velocities
    //SOH CAH TOA

    double angle = Math.toDegrees(Math.atan(y/x)) -90;

    if (x < 0) {
      angle += 180;
    }

    if (x >= 0 && y < 0) {
      angle += 360;
    }

    if(angle < 0) {
      angle += 360;
    }


    double velocity = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

    FL.move(velocity, angle);
    FR.move(velocity, angle);
    BR.move(velocity, angle);
    BL.move(velocity, angle);

    SmartDashboard.putNumber("Calculated Controller Angle", angle);
  }


  @Override
  public void periodic() {
  }
}
