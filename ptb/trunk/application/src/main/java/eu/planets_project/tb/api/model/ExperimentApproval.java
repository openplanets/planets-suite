package eu.planets_project.tb.api.model;

import java.util.Vector;

/**
 * @author alindley
 *
 */
public interface ExperimentApproval extends ExperimentPhase{
	
	public void setExplanation(String sExplanation);
	public String getExplanation();
	
	public void setDecision(String sDecision);
	public String getDecision();
	
	public void setGo(boolean go);
	public boolean getGo();
	
	/**
	 * These methods define which roles are required (a user must have) to approve this stage
	 * @param iRoleID
	 */
	public void setApprovalRequiredRole(int iRoleID);
	public void setApprovalRequiredRoles(Vector<Integer> iRoleIDs);
	public void addApprovalRequiredRole(int iRoleID);
	public void addApprovalRequiredRoles(Vector<Integer> iRoleIDs);
	public void removeApprovalRequiredRole(int iRoleID);
	public void removeApprovalRequiredRoles(Vector<Integer> iRoleIDs);
	public Vector<Integer> getApprovalRequiredRoles();
	
	
	/**
	 * A concrete User instance may only be added if at least one approvalRequiredRole has been added before
	 * @param user
	 */
	public void addApprovalUser(String user);
	public void addApprovalUsers(Vector<String> users);
	public void removeApprovalUser(String user);
	public void removeApprovalUsers(Vector<String> users);
	/**
	 * Returns a list of user IDs
	 * @return
	 */
	public Vector<String> getApprovalUsersIDs();

}