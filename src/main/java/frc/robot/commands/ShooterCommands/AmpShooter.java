package frc.robot.commands.ShooterCommands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.subsystems.ShooterSubsystem;

public class AmpShooter extends Command {
    private final ShooterSubsystem m_shooterSubsystem;

    /**
     * Run the shooter backwards
     * 
     * @param shooterSubsystem
     */
    public AmpShooter(ShooterSubsystem shooterSubsystem) {
        m_shooterSubsystem = shooterSubsystem;
        addRequirements(m_shooterSubsystem);
    }

    @Override
    public void execute(){
        m_shooterSubsystem.setRPM(Constants.ShooterConstants.kAmpRPM);
    }

    @Override
    public boolean isFinished() {
        return false; //TODO
    }

    @Override
    public void end(boolean interrupted) {
        m_shooterSubsystem.setRPM(0);
    }
}
