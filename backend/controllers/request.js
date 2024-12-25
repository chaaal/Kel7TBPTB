const { KPRequest, Group, GroupMember, Member, Reply } = require("../models");
const { sequelize } = require("../models");

const { Op } = require("sequelize");
const fs = require("fs");
const path = require("path");

// Get all KP requests
const getRequests = async (req, res) => {
  try {
    const requests = await KPRequest.findAll({
      include: [
        {
          model: Group,
          include: [
            {
              model: Member,
              through: GroupMember,
            },
          ],
        },
      ],
      order: [["createdAt", "DESC"]],
    });

    return res.status(200).json({
      success: true,
      data: requests,
    });
  } catch (error) {
    console.error("Error fetching requests:", error);
    return res.status(500).json({
      success: false,
      message: "Failed to fetch KP requests",
      error: error.message,
    });
  }
};

// Get specific KP request by ID

const getRequest = async (req, res) => {
  try {
    const { id } = req.params;
    console.log(id);
    const request = await KPRequest.findOne({
      where: { id },

      include: [
        {
          model: Group,
          include: [
            {
              model: Member,
              through: GroupMember,
            },
          ],
        },
      ],
    });

    if (!request) {
      return res.status(404).json({
        success: false,

        message: "KP request not found",
      });
    }


    return res.status(200).json({
      success: true,

      data: { request },
    });



  } catch (error) {
    console.error("Error fetching request:", error);

    return res.status(500).json({
      success: false,

      message: "Failed to fetch KP request",

      error: error.message,
    });
  }
};

// Reject KP request
const rejectRequest = async (req, res) => {
  try {
    const { id } = req.params;
    const { reason } = req.body; // Optional rejection reason

    const request = await KPRequest.findByPk(id);

    if (!request) {
      return res.status(404).json({
        success: false,
        message: "KP request not found",
      });
    }

    if (request.status !== "Pending") {
      return res.status(400).json({
        success: false,
        message: "Can only reject pending requests",
      });
    }

    await request.update({
      status: "Rejected",
      reason: reason || null,
    });

    return res.status(200).json({
      success: true,
      message: "KP request rejected successfully",
      data: request,
    });
  } catch (error) {
    console.error("Error rejecting request:", error);
    return res.status(500).json({
      success: false,
      message: "Failed to reject KP request",
      error: error.message,
    });
  }
};

// Accept KP request
const acceptRequest = async (req, res) => {
  try {
    const { id } = req.params;

    const request = await KPRequest.findByPk(id);

    if (!request) {
      return res.status(404).json({
        success: false,
        message: "KP request not found",
      });
    }

    if (request.status !== "Pending") {
      return res.status(400).json({
        success: false,
        message: "Can only accept pending requests",
      });
    }

    try {
      await request.update(
        {
          status: "Accepted",
        }
      );

      return res.status(200).json({
        success: true,
        message: "KP request accepted successfully",
        data: request,
      });
    } catch (error) {
      await t.rollback();
      throw error;
    }
  } catch (error) {
    console.error("Error accepting request:", error);
    return res.status(500).json({
      success: false,
      message: "Failed to accept KP request",
      error: error.message,
    });
  }
};

// Download KP request documents
const downloadRequest = async (req, res) => {
  try {
    const { id } = req.params;

    const request = await KPRequest.findByPk(id);

    if (!request) {
      return res.status(404).json({
        success: false,
        message: "KP request not found",
      });
    }

    if (!request.proposalUrl) {
      return res.status(404).json({
        success: false,
        message: "No proposal document found for this request",
      });
    }

    // Assuming proposalUrl contains the file path relative to your storage directory
    const filePath = path.join(
      __dirname,
      "..",
      "public",
      "proposal",
      request.proposalUrl
    );

    console.log("File path:", filePath);
    // Check if file exists
    if (!fs.existsSync(filePath)) {
      return res.status(404).json({
        success: false,
        message: "Proposal file not found on server",
      });
    }

    // Send file for download
    res.download(filePath, `KP_Proposal_${request.memberId}.pdf`, (err) => {
      if (err) {
        console.error("Error downloading file:", err);
        return res.status(500).json({
          success: false,
          message: "Error downloading file",
          error: err.message,
        });
      }
    });
  } catch (error) {
    console.error("Error processing download:", error);
    return res.status(500).json({
      success: false,
      message: "Failed to process download request",
      error: error.message,
    });
  }
};

module.exports = {
  getRequests,
  getRequest,
  rejectRequest,
  acceptRequest,
  downloadRequest,
};
