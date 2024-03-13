// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants.OIConstants;
import frc.robot.commands.DriveAndOrientToTarget;
import frc.robot.commands.DriveCommand;
import frc.robot.commands.FollowAndIntake;
import frc.robot.commands.GyroReset;
import frc.robot.commands.WheelsX;
import frc.robot.commands.IntakeCommands.IntakeThenPulse;
import frc.robot.commands.IntakeCommands.IntakePositionCommand;
import frc.robot.commands.IntakeCommands.IntakeSpeedCommand;
import frc.robot.commands.IntakeCommands.Pulse;
import frc.robot.commands.LiftCommands.ManualLiftCommand;
import frc.robot.commands.ShooterCommands.SpinDownCommand;
import frc.robot.subsystems.ClimberSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.IntakeCameraSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.LiftSubsystem;
import frc.robot.subsystems.ShooterCameraSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.utils.FilteredButton;
import frc.robot.utils.FilteredController;
import frc.robot.utils.FilteredJoystick;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
        // autochooser
        private final SendableChooser<Command> autoChooser;

        // Initialize subsystems
        public final static DriveSubsystem m_robotDrive = new DriveSubsystem();
        public final static IntakeSubsystem m_intakeSubsystem = new IntakeSubsystem();
        public final static ShooterSubsystem m_shooterSubsystem = new ShooterSubsystem();
        public final static LiftSubsystem m_liftSubsystem = new LiftSubsystem();
        private final static ClimberSubsystem m_climberSubsystem = new ClimberSubsystem();
        private final static ShooterCameraSubsystem m_shooterCamera = new ShooterCameraSubsystem();
        private final static IntakeCameraSubsystem m_intakeCamera = new IntakeCameraSubsystem();

        // initialize the controllers
        // the one on the left
        private static FilteredJoystick m_leftJoystick = new FilteredJoystick(Constants.OIConstants.kLeftJoystickPort);

        // the one on the right
        private static FilteredJoystick m_rightJoystick = new FilteredJoystick(
                        Constants.OIConstants.kRightJoystickPort);

        // da buttons
        private static FilteredButton m_buttons = new FilteredButton(OIConstants.kButtonPort);

        // da operator controller
        private static FilteredController m_operatorController = new FilteredController(
                        Constants.OIConstants.kOperatorControllerPort);

        /**
         * The container for the robot. Contains subsystems, OI devices, and commands.
         */
        public RobotContainer() {

                // name commands for use in pathPlanner
                NamedCommands.registerCommand("FollowAndIntake",
                                new FollowAndIntake(m_intakeSubsystem, m_robotDrive, m_intakeCamera, m_liftSubsystem,
                                                m_shooterSubsystem));
                // NamedCommands.registerCommand("ShootFromRight",
                // new Shoot(null, null, m_intakeSubsystem, m_shooterSubsystem,
                // m_liftSubsystem));
                // NamedCommands.registerCommand("ShootFromLeft",
                // new Shoot(null, null, m_intakeSubsystem, m_shooterSubsystem,
                // m_liftSubsystem));
                // NamedCommands.registerCommand("ShootFromMiddle",
                // new Shoot(null, null, m_intakeSubsystem, m_shooterSubsystem,
                // m_liftSubsystem));
                // Configure the button bindings
                configureButtonBindings();

                // set default command for drive
                // TODO: inversion may be needed
                m_robotDrive.setDefaultCommand(new DriveCommand(m_robotDrive, () -> -1.0 * m_rightJoystick.getX(),
                                () -> -1.0 * m_rightJoystick.getY(), m_leftJoystick::getX,
                                () -> (!m_rightJoystick.getTriggerActive() && !m_buttons.getTopSwitch()),
                                Constants.DriveConstants.kRateLimitsEnabled, m_rightJoystick::getButtonTwo,
                                m_rightJoystick::getThrottle));

                // default command for lift
                m_liftSubsystem.setDefaultCommand(
                                new ManualLiftCommand(m_operatorController::getYLeft, m_liftSubsystem));

                // default command for climber
                m_climberSubsystem
                                .setDefaultCommand(
                                                m_climberSubsystem.run(() -> m_climberSubsystem.setSpeeds(0.0, false)));
                // default command for intake
                // m_intakeSubsystem.setDefaultCommand(new IntakePositionCommand(
                // () -> Constants.IntakeConstants.kIntakeStowedPosition, m_intakeSubsystem));

                // Configure the AutoBuilder last
                AutoBuilder.configureHolonomic(
                                m_robotDrive::getPose,
                                m_robotDrive::resetOdometry,
                                m_robotDrive::getRobotRelativeSpeeds,
                                m_robotDrive::drive,
                                Constants.AutoConstants.kPathFollowerConfig,
                                () -> Robot.allianceColor,
                                m_robotDrive);

                autoChooser = AutoBuilder.buildAutoChooser();

                SmartDashboard.putString("Starting Pose", m_robotDrive.getPose().toString());

                SmartDashboard.putData("Auto Chooser", autoChooser);

        }

        /**
         * Use this method to define your button -> command mappings. Buttons can be
         * created by
         * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its
         * subclasses ({@link
         * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling
         * passing it to a
         * {@link JoystickButton}.
         */
        private void configureButtonBindings() {

                // // top left button and x button on controller sets wheels to x
                new Trigger(m_buttons::getOneA).or(
                                m_rightJoystick::getButtonSeven).whileTrue(new WheelsX(m_robotDrive));

                // // top right button resets gyro or right button five
                new Trigger(m_buttons::getOneC).or(m_rightJoystick::getButtonFive).onTrue(new GyroReset());

                // // set speeds for the shooter
                new Trigger(m_operatorController::getAButton).onTrue(new SpinDownCommand(m_shooterSubsystem));

                // set intake speeds
                new Trigger(m_operatorController::getLeftTriggerActive)
                                .whileTrue(new IntakeSpeedCommand(() -> -0.6, m_intakeSubsystem));
                new Trigger(m_operatorController::getRightTriggerActive)
                                .whileTrue(new IntakeSpeedCommand(() -> Constants.IntakeConstants.kIntakeSpeed,
                                                m_intakeSubsystem));

                // set intake positions
                new Trigger(m_operatorController::getLeftBumper)
                                .onTrue(new IntakePositionCommand(() -> Constants.IntakeConstants.kIntakeStowedPosition,
                                                m_intakeSubsystem));
                new Trigger(m_operatorController::getRightBumper)
                                .whileTrue(new IntakeThenPulse(m_intakeSubsystem, m_liftSubsystem,
                                                m_shooterSubsystem));

                new Trigger(m_leftJoystick::getTriggerActive)
                                .whileTrue(new DriveAndOrientToTarget(m_robotDrive, m_intakeCamera,
                                                m_rightJoystick::getX,
                                                m_rightJoystick::getY, m_leftJoystick::getX,
                                                () -> (!m_rightJoystick.getTriggerActive()
                                                                && !m_buttons.getTopSwitch()),
                                                Constants.DriveConstants.kRateLimitsEnabled,
                                                m_rightJoystick::getButtonTwo,
                                                m_rightJoystick::getThrottle));

                // climber bindings

                // zeroes
                new Trigger(m_leftJoystick::getButtonFour).and(m_leftJoystick::getButtonThree)
                                .onTrue(m_climberSubsystem.run(() -> m_climberSubsystem.zeroLeft()));

                new Trigger(m_leftJoystick::getButtonFive).and(m_leftJoystick::getButtonThree)
                                .onTrue(m_climberSubsystem.run(() -> m_climberSubsystem.zeroRight()));

                // controls both
                new Trigger(m_leftJoystick::getButtonEight).whileTrue(m_climberSubsystem
                                .run(() -> m_climberSubsystem.setSpeeds(-0.4,
                                                false)));

                new Trigger(m_leftJoystick::getButtonNine).whileTrue(m_climberSubsystem
                                .run(() -> m_climberSubsystem.setSpeeds(1.0, false)));

                // controls left
                new Trigger(m_leftJoystick::getButtonSix).whileTrue(m_climberSubsystem
                                .run(() -> m_climberSubsystem.setLeftSpeed(Constants.ClimberConstants.kSlowSpeed,
                                                m_leftJoystick.getButtonThree())));
                new Trigger(m_leftJoystick::getButtonSeven).whileTrue(m_climberSubsystem
                                .run(() -> m_climberSubsystem.setLeftSpeed(-Constants.ClimberConstants.kSlowSpeed,
                                                m_leftJoystick.getButtonThree())));

                // controls right
                new Trigger(m_leftJoystick::getButtonTen).whileTrue(m_climberSubsystem
                                .run(() -> m_climberSubsystem.setRightSpeed(-Constants.ClimberConstants.kSlowSpeed,
                                                m_leftJoystick.getButtonThree())));
                new Trigger(m_leftJoystick::getButtonEleven).whileTrue(m_climberSubsystem
                                .run(() -> m_climberSubsystem
                                                .setRightSpeed(Constants.ClimberConstants.kSlowSpeed,
                                                                m_leftJoystick.getButtonThree())));

                // follow and intake note: this is a test
                new Trigger(m_operatorController::getLeftStickPressed)
                                .onTrue(new FollowAndIntake(m_intakeSubsystem, m_robotDrive, m_intakeCamera,
                                                m_liftSubsystem, m_shooterSubsystem));

                // zero absolute encoder lift
                new Trigger(m_operatorController::getBackButton).and(m_operatorController::getStartButton)
                                .onTrue(m_liftSubsystem.runOnce(() -> m_liftSubsystem.zero()));

                new Trigger(m_operatorController::getRightStickPressed).onTrue(new Pulse(m_intakeSubsystem));
        }

        public Command getAutonomousCommand() {
                return autoChooser.getSelected();
        }

}